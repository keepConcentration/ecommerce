package com.phm.ecommerce.domain.user.exception;

import com.phm.ecommerce.domain.common.exception.BaseException;

public class UserNotFoundException extends BaseException {

  public UserNotFoundException() {
    super(UserErrorCode.USER_NOT_FOUND);
  }

  public UserNotFoundException(Long userId) {
    super(UserErrorCode.USER_NOT_FOUND, "사용자가 존재하지 않습니다. userId: " + userId);
  }
}
