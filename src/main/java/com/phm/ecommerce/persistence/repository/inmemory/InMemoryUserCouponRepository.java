package com.phm.ecommerce.persistence.repository.inmemory;

import com.phm.ecommerce.domain.coupon.UserCoupon;
import com.phm.ecommerce.persistence.repository.UserCouponRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryUserCouponRepository implements UserCouponRepository {

  private final Map<Long, UserCoupon> store = new ConcurrentHashMap<>();
  private final AtomicLong idGenerator = new AtomicLong(1);

  @Override
  public UserCoupon save(UserCoupon userCoupon) {
    if (userCoupon.getId() == null) {
      UserCoupon newUserCoupon =
          new UserCoupon(
              idGenerator.getAndIncrement(),
              userCoupon.getUserId(),
              userCoupon.getCouponId(),
              userCoupon.getIssuedAt(),
              userCoupon.getUsedAt(),
              userCoupon.getExpiredAt());
      store.put(newUserCoupon.getId(), newUserCoupon);
      return newUserCoupon;
    }
    store.put(userCoupon.getId(), userCoupon);
    return userCoupon;
  }

  @Override
  public Optional<UserCoupon> findById(Long id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public List<UserCoupon> findByUserId(Long userId) {
    return store.values().stream().filter(coupon -> coupon.getUserId().equals(userId)).toList();
  }

  @Override
  public List<UserCoupon> findByCouponId(Long couponId) {
    return store.values().stream().filter(coupon -> coupon.getCouponId().equals(couponId)).toList();
  }

  @Override
  public boolean existsByUserIdAndCouponId(Long userId, Long couponId) {
    return store.values().stream()
        .anyMatch(
            coupon -> coupon.getUserId().equals(userId) && coupon.getCouponId().equals(couponId));
  }

  @Override
  public void deleteById(Long id) {
    store.remove(id);
  }
}
