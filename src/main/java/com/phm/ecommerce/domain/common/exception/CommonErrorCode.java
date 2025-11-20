package com.phm.ecommerce.domain.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
  INVALID_INPUT("INVALID_INPUT", "입력값이 올바르지 않습니다", HttpStatus.BAD_REQUEST),
  MISSING_PARAMETER("MISSING_PARAMETER", "필수 파라미터가 누락되었습니다", HttpStatus.BAD_REQUEST),
  TYPE_MISMATCH("TYPE_MISMATCH", "잘못된 데이터 타입입니다", HttpStatus.BAD_REQUEST),

  METHOD_NOT_ALLOWED("METHOD_NOT_ALLOWED", "지원하지 않는 HTTP 메서드입니다", HttpStatus.METHOD_NOT_ALLOWED),

  CONFLICT("CONFLICT", "리소스 충돌이 발생했습니다", HttpStatus.CONFLICT),
  LOCK_ACQUISITION_FAILED("LOCK_ACQUISITION_FAILED", "동시 처리 중 대기 시간을 초과했습니다. 잠시 후 다시 시도해주세요", HttpStatus.CONFLICT),

  INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요", HttpStatus.INTERNAL_SERVER_ERROR),
  ;

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
