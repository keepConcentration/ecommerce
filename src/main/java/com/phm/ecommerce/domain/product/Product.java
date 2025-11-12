package com.phm.ecommerce.domain.product;

import com.phm.ecommerce.domain.common.BaseEntity;
import com.phm.ecommerce.domain.product.exception.InsufficientStockException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Entity
@Table(name = "products")
@Slf4j
@Getter
public class Product extends BaseEntity {

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private Long price;

  @Column(nullable = false)
  private Long quantity;

  @Column(nullable = false)
  private Long viewCount;

  @Column(nullable = false)
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
  }

  public void increaseStock(Long stock) {
    this.quantity += stock;
  }

  public boolean hasEnoughStock(Long requestedQuantity) {
    return this.quantity >= requestedQuantity;
  }

  public void increaseSalesCount(Long count) {
    this.salesCount += count;
    log.debug("판매량 증가 - productId: {}, count: {}, totalSalesCount: {}",
        this.getId(), count, this.salesCount);
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
  }

  public Double calculatePopularityScore(Double viewWeight, Double salesWeight) {
    return (this.viewCount * viewWeight) + (this.salesCount * salesWeight);
  }
}
