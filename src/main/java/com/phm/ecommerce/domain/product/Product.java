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
  private Long salesCount;

  protected Product() {
    super();
  }

  private Product(Long id, String name, Long price, Long quantity, Long viewCount, Long salesCount) {
    super(id);
    this.name = name;
    this.price = price;
    this.quantity = quantity;
    this.viewCount = viewCount != null ? viewCount : 0L;
    this.salesCount = salesCount != null ? salesCount : 0L;
  }

  public static Product create(String name, Long price, Long quantity) {
    return new Product(null, name, price, quantity, 0L, 0L);
  }

  public static Product reconstruct(Long id, String name, Long price, Long quantity, Long viewCount, Long salesCount) {
    return new Product(id, name, price, quantity, viewCount, salesCount);
  }

  public void increaseViewCount() {
    this.viewCount++;
    updateTimestamp();
  }

  public void decreaseStock(Long stock) {
    if (!hasEnoughStock(stock)) {
      log.warn("재고 부족 - productId: {}, requestedStock: {}, currentStock: {}",
          this.getId(), stock, this.quantity);
      throw new InsufficientStockException(this.getId(), stock, this.quantity);
    }
    this.quantity -= stock;
    log.debug("재고 차감 - productId: {}, stock: {}, remainingStock: {}",
        this.getId(), stock, this.quantity);
    updateTimestamp();
  }

  public void increaseStock(Long stock) {
    this.quantity += stock;
    updateTimestamp();
  }

  public boolean hasEnoughStock(Long requestedQuantity) {
    return this.quantity >= requestedQuantity;
  }

  public void increaseSalesCount(Long count) {
    this.salesCount += count;
    log.debug("판매량 증가 - productId: {}, count: {}, totalSalesCount: {}",
        this.getId(), count, this.salesCount);
    updateTimestamp();
  }

  public void decreaseSalesCount(Long count) {
    if (this.salesCount >= count) {
      this.salesCount -= count;
      log.debug("판매량 감소 (롤백) - productId: {}, count: {}, totalSalesCount: {}",
          this.getId(), count, this.salesCount);
    } else {
      log.warn("판매량 롤백 불가 - productId: {}, requestedCount: {}, currentSalesCount: {}",
          this.getId(), count, this.salesCount);
      this.salesCount = 0L;
    }
    updateTimestamp();
  }

  public Double calculatePopularityScore(Double viewWeight, Double salesWeight) {
    return (this.viewCount * viewWeight) + (this.salesCount * salesWeight);
  }
}
