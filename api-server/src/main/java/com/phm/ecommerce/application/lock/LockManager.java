package com.phm.ecommerce.application.lock;

import java.util.function.Supplier;

public interface LockManager {

  <T> T executeWithLock(String lockKey, Supplier<T> task);

  default void executeWithLock(String lockKey, Runnable task) {
    executeWithLock(lockKey, () -> {
      task.run();
      return null;
    });
  }
}
