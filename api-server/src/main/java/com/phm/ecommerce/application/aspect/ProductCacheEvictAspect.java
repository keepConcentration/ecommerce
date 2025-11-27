package com.phm.ecommerce.application.aspect;

import com.phm.ecommerce.domain.product.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ProductCacheEvictAspect {

  private final CacheManager cacheManager;

  @AfterReturning(
      pointcut = "execution(* com.phm.ecommerce.infrastructure.repository.ProductRepository.save(..)) && args(product)",
      returning = "savedProduct"
  )
  public void evictProductCacheAfterSave(Product product, Product savedProduct) {
    Long productId = savedProduct.getId();
    log.info("상품 저장 - 캐시 무효화: productId={}", productId);

    Cache productCache = cacheManager.getCache("product");
    if (productCache != null) {
      productCache.evict(productId);
    }
  }
}
