package com.phm.ecommerce.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phm.ecommerce.application.service.ExternalOrderService;
import com.phm.ecommerce.domain.order.event.OrderCreatedEvent;
import com.phm.ecommerce.infrastructure.dlq.DeadLetterMessage;
import com.phm.ecommerce.infrastructure.dlq.RedisDLQService;
import com.phm.ecommerce.support.TestContainerSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class DLQRetryBatchJobTest extends TestContainerSupport {

  @Autowired
  private JobLauncher jobLauncher;

  @Autowired
  private Job dlqRetryJob;

  @Autowired
  private RedisDLQService dlqService;

  @Autowired
  private ObjectMapper redisObjectMapper;

  @MockBean
  private ExternalOrderService externalOrderService;

  @BeforeEach
  void setUp() {
    // DLQ 초기화
    List<DeadLetterMessage> messages = dlqService.getRetryableMessages();
    messages.forEach(msg -> dlqService.removeMessage(msg.id()));
  }

  @Test
  @DisplayName("DLQ 재시도 배치가 성공적으로 실행되어야 한다")
  void dlqRetryBatchJobShouldComplete() throws Exception {
    // Given: DLQ에 재시도할 메시지 추가
    OrderCreatedEvent event = new OrderCreatedEvent(1L, 1L, 50000L, LocalDateTime.now());
    String eventJson = redisObjectMapper.writeValueAsString(event);

    DeadLetterMessage message = DeadLetterMessage.create(
        "test-message-1",
        eventJson,
        "ExternalOrderService",
        "Network timeout"
    );
    dlqService.addToDeadLetterQueue(message);

    // When: 배치 Job 실행
    JobParameters jobParameters = new JobParametersBuilder()
        .addLong("timestamp", System.currentTimeMillis())
        .toJobParameters();

    JobExecution jobExecution = jobLauncher.run(dlqRetryJob, jobParameters);

    // Then: Job이 성공적으로 완료되어야 함
    assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    // And: 외부 서비스가 호출되어야 함
    verify(externalOrderService, times(1))
        .sendOrderToExternalSystem(anyLong(), anyLong(), anyLong(), any(LocalDateTime.class));

    // And: 성공한 메시지는 DLQ에서 제거되어야 함
    List<DeadLetterMessage> remainingMessages = dlqService.getRetryableMessages();
    assertThat(remainingMessages).isEmpty();
  }

  @Test
  @DisplayName("재시도 가능한 메시지가 없으면 아무 작업 없이 완료되어야 한다")
  void dlqRetryBatchJobShouldCompleteWithNoMessages() throws Exception {
    // Given: DLQ가 비어있는 상태

    // When: 배치 Job 실행
    JobParameters jobParameters = new JobParametersBuilder()
        .addLong("timestamp", System.currentTimeMillis())
        .toJobParameters();

    JobExecution jobExecution = jobLauncher.run(dlqRetryJob, jobParameters);

    // Then: Job이 성공적으로 완료되어야 함
    assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    // And: 외부 서비스가 호출되지 않아야 함
    verify(externalOrderService, times(0))
        .sendOrderToExternalSystem(anyLong(), anyLong(), anyLong(), any(LocalDateTime.class));
  }

  @Test
  @DisplayName("재시도 실패 시 재시도 카운트가 증가해야 한다")
  void dlqRetryBatchJobShouldIncrementRetryCountOnFailure() throws Exception {
    // Given: DLQ에 메시지 추가
    OrderCreatedEvent event = new OrderCreatedEvent(1L, 1L, 50000L, LocalDateTime.now());
    String eventJson = redisObjectMapper.writeValueAsString(event);

    DeadLetterMessage message = DeadLetterMessage.create(
        "test-message-2",
        eventJson,
        "ExternalOrderService",
        "Network timeout"
    );
    dlqService.addToDeadLetterQueue(message);

    // And: 외부 서비스가 실패하도록 설정
    doThrow(new RuntimeException("External system unavailable"))
        .when(externalOrderService)
        .sendOrderToExternalSystem(anyLong(), anyLong(), anyLong(), any(LocalDateTime.class));

    // When: 배치 Job 실행
    JobParameters jobParameters = new JobParametersBuilder()
        .addLong("timestamp", System.currentTimeMillis())
        .toJobParameters();

    JobExecution jobExecution = jobLauncher.run(dlqRetryJob, jobParameters);

    // Then: Job이 성공적으로 완료되어야 함 (일부 메시지 실패는 Job 실패로 이어지지 않음)
    assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    // And: 재시도 카운트가 증가해야 함
    List<DeadLetterMessage> remainingMessages = dlqService.getRetryableMessages();
    assertThat(remainingMessages).hasSize(1);
    assertThat(remainingMessages.get(0).retryCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("재시도 횟수 초과 시 메시지가 제거되어야 한다")
  void dlqRetryBatchJobShouldRemoveMessageAfterMaxRetries() throws Exception {
    // Given: 재시도 횟수가 최대치에 도달한 메시지
    OrderCreatedEvent event = new OrderCreatedEvent(1L, 1L, 50000L, LocalDateTime.now());
    String eventJson = redisObjectMapper.writeValueAsString(event);

    DeadLetterMessage message = DeadLetterMessage.create(
        "test-message-3",
        eventJson,
        "ExternalOrderService",
        "Network timeout"
    );

    // 재시도 카운트를 최대치로 설정
    for (int i = 0; i < 3; i++) {
      message = message.incrementRetryCount();
    }
    dlqService.addToDeadLetterQueue(message);

    // When: 배치 Job 실행
    JobParameters jobParameters = new JobParametersBuilder()
        .addLong("timestamp", System.currentTimeMillis())
        .toJobParameters();

    JobExecution jobExecution = jobLauncher.run(dlqRetryJob, jobParameters);

    // Then: Job이 성공적으로 완료되어야 함
    assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    // And: 재시도 횟수 초과로 메시지가 제거되어야 함
    List<DeadLetterMessage> remainingMessages = dlqService.getRetryableMessages();
    assertThat(remainingMessages).isEmpty();

    // And: 외부 서비스가 호출되지 않아야 함 (재시도 횟수 초과로 스킵)
    verify(externalOrderService, times(0))
        .sendOrderToExternalSystem(anyLong(), anyLong(), anyLong(), any(LocalDateTime.class));
  }

  @Test
  @DisplayName("여러 메시지를 배치로 처리해야 한다")
  void dlqRetryBatchJobShouldProcessMultipleMessages() throws Exception {
    // Given: 여러 메시지를 DLQ에 추가
    for (int i = 1; i <= 5; i++) {
      OrderCreatedEvent event = new OrderCreatedEvent((long) i, (long) i, 50000L, LocalDateTime.now());
      String eventJson = redisObjectMapper.writeValueAsString(event);

      DeadLetterMessage message = DeadLetterMessage.create(
          "test-message-" + i,
          eventJson,
          "ExternalOrderService",
          "Network timeout"
      );
      dlqService.addToDeadLetterQueue(message);
    }

    // When: 배치 Job 실행
    JobParameters jobParameters = new JobParametersBuilder()
        .addLong("timestamp", System.currentTimeMillis())
        .toJobParameters();

    JobExecution jobExecution = jobLauncher.run(dlqRetryJob, jobParameters);

    // Then: Job이 성공적으로 완료되어야 함
    assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    // And: 모든 메시지가 처리되어야 함
    verify(externalOrderService, times(5))
        .sendOrderToExternalSystem(anyLong(), anyLong(), anyLong(), any(LocalDateTime.class));

    // And: 모든 메시지가 DLQ에서 제거되어야 함
    List<DeadLetterMessage> remainingMessages = dlqService.getRetryableMessages();
    assertThat(remainingMessages).isEmpty();
  }
}
