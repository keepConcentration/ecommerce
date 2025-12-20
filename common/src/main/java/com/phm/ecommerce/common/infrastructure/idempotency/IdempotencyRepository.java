package com.phm.ecommerce.common.infrastructure.idempotency;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for idempotency records.
 */
public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord, Long> {

    /**
     * Find idempotency record by key.
     */
    Optional<IdempotencyRecord> findByIdempotencyKey(String idempotencyKey);

    /**
     * Check if idempotency key exists.
     */
    boolean existsByIdempotencyKey(String idempotencyKey);

    /**
     * Delete expired idempotency records (for cleanup).
     */
    @Modifying
    @Query("DELETE FROM IdempotencyRecord i WHERE i.expiresAt < :now")
    void deleteExpiredRecords(@Param("now") LocalDateTime now);
}
