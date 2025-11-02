package com.phm.ecommerce.persistence.repository;


import com.phm.ecommerce.domain.coupon.UserCoupon;
import java.util.List;
import java.util.Optional;

public interface UserCouponRepository {

  UserCoupon save(UserCoupon userCoupon);

  Optional<UserCoupon> findById(Long id);

  List<UserCoupon> findByUserId(Long userId);

  boolean existsByUserIdAndCouponId(Long userId, Long couponId);

  void deleteById(Long id);
}
