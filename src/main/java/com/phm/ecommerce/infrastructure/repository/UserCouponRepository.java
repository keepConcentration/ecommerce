package com.phm.ecommerce.infrastructure.repository;


import com.phm.ecommerce.domain.coupon.UserCoupon;
import com.phm.ecommerce.domain.coupon.exception.CouponNotFoundException;

import java.util.List;
import java.util.Optional;

public interface UserCouponRepository {

  UserCoupon save(UserCoupon userCoupon);

  Optional<UserCoupon> findById(Long id);

  default UserCoupon findByIdOrThrow(Long id) {
    return findById(id).orElseThrow(CouponNotFoundException::new);
  }

  List<UserCoupon> findByUserId(Long userId);

  List<UserCoupon> findByCouponId(Long couponId);

  boolean existsByUserIdAndCouponId(Long userId, Long couponId);

  void deleteById(Long id);
}
