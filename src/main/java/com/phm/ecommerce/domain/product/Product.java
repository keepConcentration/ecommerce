package com.phm.ecommerce.domain.product;

import com.phm.ecommerce.domain.common.BaseEntity;
import com.phm.ecommerce.domain.product.exception.InsufficientStockException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class Product extends BaseEntity {

  private String name;
  private Long price;
  private Long quantity;
  private Long viewCount;

  protected Product() {
    super();
  }

  private Product(Long id, String name, Long price, Long quantity, Long viewCount) {
    super(id);
    this.name = name;
    this.price = price;
    this.quantity = quantity;
    this.viewCount = viewCount != null ? viewCount : 0L;
  }

  public static Product create(String name, Long price, Long quantity) {
    return new Product(null, name, price, quantity, 0L);
  }

  public static Product reconstruct(Long id, String name, Long price, Long quantity, Long viewCount) {
    return new Product(id, name, price, quantity, viewCount);
  }

  public void increaseViewCount() {
    this.viewCount++;
    updateTimestamp();
  }

  public void decreaseStock(Long amount) {
    if (!hasEnoughStock(amount)) {
      log.warn("재고 부족 - productId: {}, requestedAmount: {}, currentStock: {}",
          this.getId(), amount, this.quantity);
      throw new InsufficientStockException(this.getId(), amount, this.quantity);
    }
    this.quantity -= amount;
    log.debug("재고 차감 - productId: {}, amount: {}, remainingStock: {}",
        this.getId(), amount, this.quantity);
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
