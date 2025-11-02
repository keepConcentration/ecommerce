package com.phm.ecommerce.domain.user;

import com.phm.ecommerce.domain.common.BaseEntity;
import lombok.Getter;

@Getter
public class User extends BaseEntity {

  public User() {
    super();
  }

  public User(Long id) {
    super(id);
  }

  public static User create() {
    return new User();
  }
}
