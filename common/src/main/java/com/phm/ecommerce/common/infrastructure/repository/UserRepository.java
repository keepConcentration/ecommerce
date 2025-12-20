package com.phm.ecommerce.common.infrastructure.repository;

import com.phm.ecommerce.common.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
