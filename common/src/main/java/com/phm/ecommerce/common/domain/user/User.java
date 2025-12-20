package com.phm.ecommerce.common.domain.user;

import com.phm.ecommerce.common.domain.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "users")
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
