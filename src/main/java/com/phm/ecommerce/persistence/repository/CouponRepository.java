package com.phm.ecommerce.persistence.repository;


import com.phm.ecommerce.domain.coupon.Coupon;
import java.util.Optional;

public interface CouponRepository {

  Coupon save(Coupon coupon);

  Optional<Coupon> findById(Long id);

  void deleteById(Long id);
}
