package com.phm.ecommerce.persistence.repository.inmemory;

import com.phm.ecommerce.domain.coupon.Coupon;
import com.phm.ecommerce.persistence.repository.CouponRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryCouponRepository implements CouponRepository {

  private final Map<Long, Coupon> store = new ConcurrentHashMap<>();
  private final AtomicLong idGenerator = new AtomicLong(1);

  @Override
  public Coupon save(Coupon coupon) {
    if (coupon.getId() == null) {
      Coupon newCoupon =
          new Coupon(
              idGenerator.getAndIncrement(),
              coupon.getName(),
              coupon.getDiscountAmount(),
              coupon.getTotalQuantity(),
              coupon.getIssuedQuantity(),
              coupon.getValidDays());
      store.put(newCoupon.getId(), newCoupon);
      return newCoupon;
    }
    store.put(coupon.getId(), coupon);
    return coupon;
  }

  @Override
  public Optional<Coupon> findById(Long id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public void deleteById(Long id) {
    store.remove(id);
  }
}
