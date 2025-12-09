package com.phm.ecommerce.infrastructure.event.config;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventRejectedExecutionHandler implements RejectedExecutionHandler {

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

    // TODO: 실패 이벤트 DLQ에 저장
    log.error("이벤트 처리 최종 실패 - task: {}", task);
  }
}
