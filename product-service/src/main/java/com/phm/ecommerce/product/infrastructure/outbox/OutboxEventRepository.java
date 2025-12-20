package com.phm.ecommerce.product.infrastructure.outbox;

import com.phm.ecommerce.product.infrastructure.outbox.OutboxEvent.OutboxStatus;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for Outbox pattern events.
 */
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * Find pending events ordered by creation time for publishing.
     */
    @Query("SELECT o FROM OutboxEvent o WHERE o.status = :status ORDER BY o.createdAt ASC")
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(
        @Param("status") OutboxStatus status,
        Pageable pageable
    );

    /**
     * Find failed events that can be retried.
     */
    @Query("SELECT o FROM OutboxEvent o WHERE o.status = 'FAILED' AND o.retryCount < :maxRetries ORDER BY o.createdAt ASC")
    List<OutboxEvent> findRetryableFailedEvents(@Param("maxRetries") int maxRetries, Pageable pageable);
}
