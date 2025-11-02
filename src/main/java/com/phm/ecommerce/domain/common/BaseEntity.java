package com.phm.ecommerce.domain.common;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public abstract class BaseEntity {

  protected Long id;
  protected LocalDateTime createdAt;
  protected LocalDateTime updatedAt;

  protected BaseEntity() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  protected BaseEntity(Long id) {
    this.id = id;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  protected void setId(Long id) {
    this.id = id;
  }

  protected void updateTimestamp() {
    this.updatedAt = LocalDateTime.now();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BaseEntity that = (BaseEntity) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
