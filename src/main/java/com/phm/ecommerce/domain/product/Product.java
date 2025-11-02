package com.phm.ecommerce.domain.product;

import com.phm.ecommerce.domain.common.BaseEntity;
import lombok.Getter;

@Getter
public class Product extends BaseEntity {

  private String name;
  private Long price;
  private Long quantity;
  private Long viewCount;

  protected Product() {
    super();
    this.viewCount = 0L;
  }

  public Product(Long id, String name, Long price, Long quantity, Long viewCount) {
    super(id);
    this.name = name;
    this.price = price;
    this.quantity = quantity;
    this.viewCount = viewCount != null ? viewCount : 0L;
  }

  public static Product create(String name, Long price, Long quantity) {
    return new Product(null, name, price, quantity, 0L);
  }

  public void increaseViewCount() {
    this.viewCount++;
    updateTimestamp();
  }

  public void decreaseStock(Long amount) {
    if (this.quantity < amount) {
      throw new IllegalStateException("재고가 부족합니다");
    }
    this.quantity -= amount;
    updateTimestamp();
  }

  public void increaseStock(Long amount) {
    this.quantity += amount;
    updateTimestamp();
  }

  public boolean hasEnoughStock(Long requestedQuantity) {
    return this.quantity >= requestedQuantity;
  }
}
