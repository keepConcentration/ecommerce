package com.phm.ecommerce.infrastructure.event.config;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@EnableAsync
@Configuration
@RequiredArgsConstructor
public class AsyncConfig implements AsyncConfigurer {

  private final RejectedExecutionHandler rejectedExecutionHandler;

  @Bean(name = "eventTaskExecutor")
  @Override
  public Executor getAsyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("event-async-");
    executor.setRejectedExecutionHandler(rejectedExecutionHandler);
    executor.initialize();
    return executor;
  }

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return (ex, method, params) -> {
      log.error("비동기 이벤트 처리 예외 - method: {}, params: {}, error: {}",
          method.getName(), params, ex.getMessage(), ex);
    };
  }
}
