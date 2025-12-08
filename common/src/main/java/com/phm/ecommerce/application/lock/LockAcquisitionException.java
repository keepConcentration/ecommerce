package com.phm.ecommerce.application.lock;

public class LockAcquisitionException extends RuntimeException {

  public LockAcquisitionException(String message) {
    super(message);
  }

  public LockAcquisitionException(String message, Throwable cause) {
    super(message, cause);
  }
}
