package com.phm.ecommerce.common.infrastructure.repository;

import com.phm.ecommerce.common.domain.coupon.Coupon;
import com.phm.ecommerce.common.domain.coupon.exception.CouponNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

  default Coupon findByIdOrThrow(Long id) {
    return findById(id).orElseThrow(CouponNotFoundException::new);
  }

  @Query("SELECT c FROM Coupon c WHERE c.id IN :ids")
  List<Coupon> findAllByIds(@Param("ids") List<Long> ids);
}
