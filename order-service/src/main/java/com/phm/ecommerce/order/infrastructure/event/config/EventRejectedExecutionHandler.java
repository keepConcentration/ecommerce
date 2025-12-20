package com.phm.ecommerce.order.infrastructure.event.config;

import com.phm.ecommerce.order.infrastructure.dlq.DeadLetterMessage;
import com.phm.ecommerce.order.infrastructure.dlq.RedisDLQService;
import java.util.UUID;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventRejectedExecutionHandler implements RejectedExecutionHandler {

  private final RedisDLQService dlqService;

  @Override
  public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {
    log.warn("이벤트 처리 큐 포화 - poolSize: {}, activeCount: {}, queueSize: {}, taskCount: {}",
        executor.getPoolSize(),
        executor.getActiveCount(),
        executor.getQueue().size(),
        executor.getTaskCount());

    try {
      log.warn("큐 포화로 인한 재시도 대기 중...");
      Thread.sleep(100);

      if (!executor.isShutdown()) {
        executor.execute(task);
        log.info("큐 포화 재시도 성공");
        return;
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("큐 포화 재시도 중 인터럽트 발생", e);
    } catch (Exception e) {
      log.error("큐 포화 재시도 실패", e);
    }
    saveToDeadLetterQueue(task, executor);
  }

  private void saveToDeadLetterQueue(Runnable task, ThreadPoolExecutor executor) {
    try {
      String taskInfo = extractTaskInfo(task);
      String messageId = "rejected-task:" + UUID.randomUUID();

      DeadLetterMessage dlqMessage = DeadLetterMessage.create(
          messageId,
          taskInfo,
          "RejectedExecutionException",
          String.format("큐 포화로 인한 이벤트 처리 실패 - poolSize: %d, activeCount: %d, queueSize: %d",
              executor.getPoolSize(),
              executor.getActiveCount(),
              executor.getQueue().size())
      );

      dlqService.addToDeadLetterQueue(dlqMessage);

      log.info("큐 포화 실패 이벤트 DLQ 저장 완료 - messageId: {}, taskClass: {}",
          messageId, task.getClass().getSimpleName());

    } catch (Exception e) {
      log.error("큐 포화 실패 이벤트 DLQ 저장 실패 - taskClass: {}, error: {}",
          task.getClass().getSimpleName(), e.getMessage(), e);
    }
  }

  private String extractTaskInfo(Runnable task) {
    String taskClassName = task.getClass().getName();
    String taskString = task.toString();

    if (taskString.contains("@EventListener") || taskString.contains("OrderCreatedEvent")) {
      return String.format("EventListener Task - class: %s, info: %s",
          taskClassName, taskString);
    }

    return String.format("Task - class: %s, toString: %s",
        taskClassName,
        taskString.length() > 500 ? taskString.substring(0, 500) + "..." : taskString);
  }
}
