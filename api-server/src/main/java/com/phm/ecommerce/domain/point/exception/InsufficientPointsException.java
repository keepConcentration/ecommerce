package com.phm.ecommerce.domain.point.exception;

import com.phm.ecommerce.domain.common.exception.BaseException;

public class InsufficientPointsException extends BaseException {

  public InsufficientPointsException() {
    super(PointErrorCode.INSUFFICIENT_POINTS);
  }

  public InsufficientPointsException(Long userId, Long requiredPoints, Long availablePoints) {
    super(
        PointErrorCode.INSUFFICIENT_POINTS,
        String.format(
            "포인트가 부족합니다. userId: %d, 필요 포인트: %d, 보유 포인트: %d",
            userId, requiredPoints, availablePoints));
  }
}
