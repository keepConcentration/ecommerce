package com.phm.ecommerce.presentation.exception;

import com.phm.ecommerce.application.lock.LockAcquisitionException;
import com.phm.ecommerce.domain.common.exception.BaseException;
import com.phm.ecommerce.domain.common.exception.CommonErrorCode;
import com.phm.ecommerce.domain.common.exception.ErrorCode;
import com.phm.ecommerce.presentation.common.ApiResponse;
import com.phm.ecommerce.presentation.common.ErrorDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(BaseException.class)
  public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException e) {
    ErrorCode errorCode = e.getErrorCode();
    ErrorDetail errorDetail = new ErrorDetail(errorCode.getCode(), e.getMessage());
    ApiResponse<Void> response = ApiResponse.error(errorDetail);
    return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e) {
    String field = e.getBindingResult().getFieldError() != null
        ? e.getBindingResult().getFieldError().getField()
        : null;
    String message = e.getBindingResult().getFieldError() != null
        ? e.getBindingResult().getFieldError().getDefaultMessage()
        : CommonErrorCode.INVALID_INPUT.getMessage();

    ErrorCode errorCode = CommonErrorCode.INVALID_INPUT;
    ErrorDetail errorDetail = new ErrorDetail(errorCode.getCode(), message, field);
    ApiResponse<Void> response = ApiResponse.error(errorDetail);
    return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(
      MissingServletRequestParameterException e) {
    ErrorCode errorCode = CommonErrorCode.MISSING_PARAMETER;
    ErrorDetail errorDetail = new ErrorDetail(
        errorCode.getCode(),
        errorCode.getMessage() + ": " + e.getParameterName(),
        e.getParameterName());
    ApiResponse<Void> response = ApiResponse.error(errorDetail);
    return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException e) {
    ErrorCode errorCode = CommonErrorCode.TYPE_MISMATCH;
    ErrorDetail errorDetail = new ErrorDetail(
        errorCode.getCode(),
        errorCode.getMessage() + ": " + e.getName(),
        e.getName());
    ApiResponse<Void> response = ApiResponse.error(errorDetail);
    return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException e) {
    ErrorCode errorCode = CommonErrorCode.INVALID_INPUT;
    ErrorDetail errorDetail = new ErrorDetail(
        errorCode.getCode(),
        "요청 본문을 읽을 수 없습니다. JSON 형식을 확인해주세요",
        null);
    ApiResponse<Void> response = ApiResponse.error(errorDetail);
    return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(
      HttpRequestMethodNotSupportedException e) {
    ErrorCode errorCode = CommonErrorCode.METHOD_NOT_ALLOWED;
    ErrorDetail errorDetail = new ErrorDetail(
        errorCode.getCode(),
        errorCode.getMessage() + ": " + e.getMethod(),
        null);
    ApiResponse<Void> response = ApiResponse.error(errorDetail);
    return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
      IllegalArgumentException e) {
    log.warn("잘못된 인자 예외 발생", e);
    ErrorCode errorCode = CommonErrorCode.INVALID_INPUT;
    ErrorDetail errorDetail = new ErrorDetail(errorCode.getCode(), e.getMessage());
    ApiResponse<Void> response = ApiResponse.error(errorDetail);
    return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
  }

  @ExceptionHandler(LockAcquisitionException.class)
  public ResponseEntity<ApiResponse<Void>> handleLockAcquisitionException(
      LockAcquisitionException e) {
    log.warn("락 획득 실패", e);
    ErrorCode errorCode = CommonErrorCode.LOCK_ACQUISITION_FAILED;
    ErrorDetail errorDetail = new ErrorDetail(errorCode.getCode(), errorCode.getMessage());
    ApiResponse<Void> response = ApiResponse.error(errorDetail);
    return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
    log.error("예기치 않은 오류 발생", e);

    ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
    ErrorDetail errorDetail = new ErrorDetail(errorCode.getCode(), errorCode.getMessage());
    ApiResponse<Void> response = ApiResponse.error(errorDetail);
    return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
  }
}
