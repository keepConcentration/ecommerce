package com.phm.ecommerce.persistence.repository.inmemory;

import com.phm.ecommerce.domain.user.User;
import com.phm.ecommerce.persistence.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryUserRepository implements UserRepository {

  private final Map<Long, User> store = new ConcurrentHashMap<>();
  private final AtomicLong idGenerator = new AtomicLong(1);

  @Override
  public User save(User user) {
    if (user.getId() == null) {
      User newUser = new User(idGenerator.getAndIncrement());
      store.put(newUser.getId(), newUser);
      return newUser;
    }
    store.put(user.getId(), user);
    return user;
  }

  @Override
  public Optional<User> findById(Long id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public void deleteById(Long id) {
    store.remove(id);
  }
}
