package com.phm.ecommerce.persistence.repository;


import com.phm.ecommerce.domain.coupon.Coupon;
import com.phm.ecommerce.domain.coupon.exception.CouponNotFoundException;

import java.util.List;
import java.util.Optional;

public interface CouponRepository {

  Coupon save(Coupon coupon);

  Optional<Coupon> findById(Long id);

  default Coupon findByIdOrThrow(Long id) {
    return findById(id).orElseThrow(CouponNotFoundException::new);
  }

  List<Coupon> findAllByIds(List<Long> ids);

  void deleteById(Long id);
}
