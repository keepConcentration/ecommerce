package com.phm.ecommerce.domain.point.exception;

import com.phm.ecommerce.domain.common.exception.BaseException;

public class PointNotFoundException extends BaseException {

  public PointNotFoundException() {
    super(PointErrorCode.POINT_NOT_FOUND);
  }

  public PointNotFoundException(Long userId) {
    super(PointErrorCode.POINT_NOT_FOUND, "포인트를 찾을 수 없습니다. userId: " + userId);
  }
}
