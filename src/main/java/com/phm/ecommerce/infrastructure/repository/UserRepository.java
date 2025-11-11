package com.phm.ecommerce.infrastructure.repository;

import com.phm.ecommerce.domain.user.User;

import java.util.Optional;

public interface UserRepository {

  User save(User user);

  Optional<User> findById(Long id);

  void deleteById(Long id);
}
