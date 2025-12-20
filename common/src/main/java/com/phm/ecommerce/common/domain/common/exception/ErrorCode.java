package com.phm.ecommerce.common.domain.common.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
  String getCode();

  String getMessage();

  HttpStatus getHttpStatus();
}
