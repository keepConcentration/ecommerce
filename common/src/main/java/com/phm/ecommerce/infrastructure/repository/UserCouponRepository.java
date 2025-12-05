package com.phm.ecommerce.infrastructure.repository;

import com.phm.ecommerce.domain.coupon.UserCoupon;
import com.phm.ecommerce.domain.coupon.exception.UserCouponNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

  default UserCoupon findByIdOrThrow(Long id) {
    return findById(id).orElseThrow(UserCouponNotFoundException::new);
  }

  List<UserCoupon> findByUserId(Long userId);

  List<UserCoupon> findByCouponId(Long couponId);

  long countByCouponId(Long couponId);

  boolean existsByUserIdAndCouponId(Long userId, Long couponId);
}
