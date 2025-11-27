package com.phm.ecommerce.application.usecase.point;

import com.phm.ecommerce.application.lock.DistributedLock;
import com.phm.ecommerce.domain.point.Point;
import com.phm.ecommerce.domain.point.PointTransaction;
import com.phm.ecommerce.infrastructure.repository.PointRepository;
import com.phm.ecommerce.infrastructure.repository.PointTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChargePointsUseCase {

  private final PointRepository pointRepository;
  private final PointTransactionRepository pointTransactionRepository;

  public record Input(
      Long userId,
      Long amount) {}

  @DistributedLock(key = "'point:user:' + #request.userId()")
  @Transactional
  public Output execute(Input request) {
    log.info("포인트 충전 시작 - userId: {}, chargeAmount: {}", request.userId(), request.amount());

    Point point = pointRepository.findByUserId(request.userId())
        .orElseGet(() -> {
          log.debug("새로운 포인트 계정 생성 - userId: {}", request.userId());
          Point newPoint = Point.create(request.userId());
          return pointRepository.save(newPoint);
        });

    point.charge(request.amount());
    point = pointRepository.save(point);

    PointTransaction transaction = PointTransaction.createCharge(point.getId(), request.amount());
    transaction = pointTransactionRepository.save(transaction);

    log.info("포인트 충전 완료 - userId: {}, chargedAmount: {}, totalAmount: {}",
        request.userId(), request.amount(), point.getAmount());

    return new Output(
        point.getId(),
        point.getUserId(),
        point.getAmount(),
        request.amount(),
        transaction.getId(),
        transaction.getCreatedAt()
    );
  }

  public record Output(
      Long pointId,
      Long userId,
      Long amount,
      Long chargedAmount,
      Long transactionId,
      LocalDateTime createdAt) {}
}
