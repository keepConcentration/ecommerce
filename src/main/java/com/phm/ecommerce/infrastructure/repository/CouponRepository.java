package com.phm.ecommerce.infrastructure.repository;

import com.phm.ecommerce.domain.coupon.Coupon;
import com.phm.ecommerce.domain.coupon.exception.CouponNotFoundException;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

  default Coupon findByIdOrThrow(Long id) {
    return findById(id).orElseThrow(CouponNotFoundException::new);
  }

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT c FROM Coupon c WHERE c.id = :id")
  Optional<Coupon> findByIdWithLock(@Param("id") Long id);

  default Coupon findByIdWithLockOrThrow(Long id) {
    return findByIdWithLock(id).orElseThrow(CouponNotFoundException::new);
  }

  @Query("SELECT c FROM Coupon c WHERE c.id IN :ids")
  List<Coupon> findAllByIds(@Param("ids") List<Long> ids);
}
