package com.phm.ecommerce.persistence.repository;


import com.phm.ecommerce.domain.coupon.Coupon;
import java.util.List;
import java.util.Optional;

public interface CouponRepository {

  Coupon save(Coupon coupon);

  Optional<Coupon> findById(Long id);

  List<Coupon> findAllByIds(List<Long> ids);

  void deleteById(Long id);
}
