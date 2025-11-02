package com.phm.ecommerce.presentation.exception;

import com.phm.ecommerce.domain.common.exception.BaseException;
import com.phm.ecommerce.domain.common.exception.ErrorCode;
import com.phm.ecommerce.presentation.common.ApiResponse;
import com.phm.ecommerce.presentation.common.ErrorDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(BaseException.class)
  public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException e) {
    ErrorCode errorCode = e.getErrorCode();
    ErrorDetail errorDetail = new ErrorDetail(errorCode.getCode(), e.getMessage(), null);
    ApiResponse<Void> response = ApiResponse.error(errorDetail);
    return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
    log.error("Unexpected error occurred", e);
    ErrorDetail errorDetail = new ErrorDetail("INTERNAL_SERVER_ERROR", "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", null);
    ApiResponse<Void> response = ApiResponse.error(errorDetail);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}
