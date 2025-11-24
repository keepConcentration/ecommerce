package com.phm.ecommerce.config;

import com.phm.ecommerce.domain.cart.CartItem;
import com.phm.ecommerce.domain.coupon.Coupon;
import com.phm.ecommerce.domain.coupon.UserCoupon;
import com.phm.ecommerce.domain.order.Order;
import com.phm.ecommerce.domain.order.OrderItem;
import com.phm.ecommerce.domain.point.Point;
import com.phm.ecommerce.domain.point.PointTransaction;
import com.phm.ecommerce.domain.product.Product;
import com.phm.ecommerce.domain.user.User;
import com.phm.ecommerce.infrastructure.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 개발 환경용 대량 테스트 데이터 생성기
 *
 * 애플리케이션 시작 시 자동으로 실행되어 다음 데이터를 생성합니다:
 * - Users: 10,000명
 * - Products: 10,000개
 * - Points: 10,000개 (각 사용자마다)
 * - Coupons: 100종류
 * - UserCoupons: 50,000개
 * - CartItems: 30,000개
 * - Orders: 10,000개
 * - OrderItems: 20,000개
 * - PointTransactions: 30,000개
 *
 */
@Component
@Profile("!test")  // 테스트 환경에서는 실행하지 않음
@RequiredArgsConstructor
@Slf4j
public class DataLoader /*implements ApplicationRunner*/ {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PointRepository pointRepository;
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PointTransactionRepository pointTransactionRepository;

    private final Random random = new Random();

    // @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("========================================");
        log.info("대량 테스트 데이터 생성 시작");
        log.info("========================================");

        // 기존 데이터가 있는지 확인
        if (userRepository.count() > 0) {
            log.info("이미 데이터가 존재합니다. 데이터 생성을 건너뜁니다.");
            return;
        }

        long startTime = System.currentTimeMillis();

        // 1. 사용자 생성 (10,000명)
        List<User> users = createUsers(10000);
        log.info("✓ Users 생성 완료: {} 건", users.size());

        // 2. 상품 생성 (10,000개)
        List<Product> products = createProducts(10000);
        log.info("✓ Products 생성 완료: {} 건", products.size());

        // 3. 포인트 생성 (10,000개 - 각 사용자마다)
        List<Point> points = createPoints(users);
        log.info("✓ Points 생성 완료: {} 건", points.size());

        // 4. 포인트 충전 트랜잭션 생성 (10,000개)
        List<PointTransaction> chargeTransactions = createChargeTransactions(points);
        log.info("✓ Point Charge Transactions 생성 완료: {} 건", chargeTransactions.size());

        // 5. 쿠폰 생성 (100종류)
        List<Coupon> coupons = createCoupons(100);
        log.info("✓ Coupons 생성 완료: {} 건", coupons.size());

        // 6. 사용자 쿠폰 발급 (50,000개)
        List<UserCoupon> userCoupons = createUserCoupons(users, coupons, 50000);
        log.info("✓ UserCoupons 생성 완료: {} 건", userCoupons.size());

        // 7. 장바구니 아이템 생성 (30,000개)
        List<CartItem> cartItems = createCartItems(users, products, 30000);
        log.info("✓ CartItems 생성 완료: {} 건", cartItems.size());

        // 8. 주문 생성 (10,000개)
        List<Order> orders = createOrders(users, 10000);
        log.info("✓ Orders 생성 완료: {} 건", orders.size());

        // 9. 주문 아이템 생성 (20,000개)
        List<OrderItem> orderItems = createOrderItems(orders, users, products, userCoupons, 20000);
        log.info("✓ OrderItems 생성 완료: {} 건", orderItems.size());

        // 10. 포인트 사용 트랜잭션 생성 (주문당 1개)
        List<PointTransaction> deductionTransactions = createDeductionTransactions(orders, points, users);
        log.info("✓ Point Deduction Transactions 생성 완료: {} 건", deductionTransactions.size());

        long endTime = System.currentTimeMillis();
        long totalTime = (endTime - startTime) / 1000;

        log.info("========================================");
        log.info("대량 테스트 데이터 생성 완료!");
        log.info("총 소요 시간: {} 초", totalTime);
        log.info("========================================");
    }

    private List<User> createUsers(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            users.add(User.create());
        }
        return userRepository.saveAll(users);
    }

    private List<Product> createProducts(int count) {
        List<Product> products = new ArrayList<>();
        String[] categories = {"전자제품", "의류", "식품", "도서", "가구", "스포츠", "뷰티", "완구", "반려동물", "문구"};

        for (int i = 0; i < count; i++) {
            String category = categories[i % categories.length];
            String name = category + " 상품 " + (i + 1);
            Long price = (long) ((random.nextInt(100) + 1) * 1000); // 1,000 ~ 100,000원
            Long stock = (long) (random.nextInt(500) + 100); // 100 ~ 600개

            products.add(Product.create(name, price, stock));
        }
        return productRepository.saveAll(products);
    }

    private List<Point> createPoints(List<User> users) {
        List<Point> points = new ArrayList<>();
        for (User user : users) {
            Point point = Point.create(user.getId());
            // 초기 포인트 10,000 ~ 1,000,000원
            long initialAmount = (long) ((random.nextInt(100) + 1) * 10000);
            point.charge(initialAmount);
            points.add(point);
        }
        return pointRepository.saveAll(points);
    }

    private List<PointTransaction> createChargeTransactions(List<Point> points) {
        List<PointTransaction> transactions = new ArrayList<>();
        for (Point point : points) {
            // 각 포인트마다 충전 트랜잭션 1개 생성
            PointTransaction transaction = PointTransaction.createCharge(
                point.getId(),
                point.getAmount()
            );
            transactions.add(transaction);
        }
        return pointTransactionRepository.saveAll(transactions);
    }

    private List<Coupon> createCoupons(int count) {
        List<Coupon> coupons = new ArrayList<>();
        String[] couponTypes = {"신규가입", "생일축하", "VIP", "주말특가", "시즌특별", "재구매", "친구추천", "이벤트", "앱전용", "첫구매"};

        for (int i = 0; i < count; i++) {
            String type = couponTypes[i % couponTypes.length];
            String name = type + " 쿠폰 " + (i / couponTypes.length + 1);
            Long discountAmount = (long) ((random.nextInt(10) + 1) * 1000); // 1,000 ~ 10,000원
            Long totalQuantity = (long) (random.nextInt(4000) + 1000); // 1,000 ~ 5,000개 (충분한 수량 확보)
            Integer validDays = (random.nextInt(6) + 1) * 30; // 30 ~ 180일

            coupons.add(Coupon.create(name, discountAmount, totalQuantity, validDays));
        }
        return couponRepository.saveAll(coupons);
    }

    private List<UserCoupon> createUserCoupons(List<User> users, List<Coupon> coupons, int count) {
        List<UserCoupon> userCoupons = new ArrayList<>();
        int successCount = 0;
        int attemptCount = 0;
        int maxAttempts = count * 10; // 무한 루프 방지

        while (successCount < count && attemptCount < maxAttempts) {
            attemptCount++;

            User user = users.get(random.nextInt(users.size()));
            Coupon coupon = coupons.get(random.nextInt(coupons.size()));

            // 쿠폰이 발급 가능한지 확인
            if (!coupon.canIssue()) {
                continue; // 소진된 쿠폰이면 다른 쿠폰 선택
            }

            UserCoupon userCoupon = UserCoupon.issue(user.getId(), coupon.getId(), coupon.getValidDays());
            userCoupons.add(userCoupon);

            // 쿠폰 발급 수량 증가
            coupon.issue();
            successCount++;
        }

        if (successCount < count) {
            log.warn("목표 UserCoupon 개수({})에 도달하지 못했습니다. 실제 생성: {}", count, successCount);
        }

        couponRepository.saveAll(coupons); // 쿠폰 발급 수량 업데이트
        return userCouponRepository.saveAll(userCoupons);
    }

    private List<CartItem> createCartItems(List<User> users, List<Product> products, int count) {
        List<CartItem> cartItems = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            User user = users.get(random.nextInt(users.size()));
            Product product = products.get(random.nextInt(products.size()));
            Long quantity = (long) (random.nextInt(5) + 1); // 1 ~ 5개

            CartItem cartItem = CartItem.create(user.getId(), product.getId(), quantity);
            cartItems.add(cartItem);
        }

        return cartItemRepository.saveAll(cartItems);
    }

    private List<Order> createOrders(List<User> users, int count) {
        List<Order> orders = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            User user = users.get(random.nextInt(users.size()));
            Long totalAmount = (long) ((random.nextInt(100) + 10) * 1000); // 10,000 ~ 110,000원
            Long discountAmount = (long) (random.nextInt(10) * 1000); // 0 ~ 9,000원

            Order order = Order.create(user.getId(), totalAmount, discountAmount);
            orders.add(order);
        }

        return orderRepository.saveAll(orders);
    }

    private List<OrderItem> createOrderItems(
        List<Order> orders,
        List<User> users,
        List<Product> products,
        List<UserCoupon> userCoupons,
        int count
    ) {
        List<OrderItem> orderItems = new ArrayList<>();
        Set<Product> modifiedProducts = new HashSet<>(); // 변경된 상품만 추적

        for (int i = 0; i < count; i++) {
            Order order = orders.get(i % orders.size());
            Product product = products.get(random.nextInt(products.size()));
            Long quantity = (long) (random.nextInt(3) + 1); // 1 ~ 3개
            Long discountAmount = (long) (random.nextInt(5) * 1000); // 0 ~ 4,000원

            // 랜덤으로 쿠폰 사용 여부 결정 (30% 확률)
            Long userCouponId = null;
            if (random.nextDouble() < 0.3 && !userCoupons.isEmpty()) {
                UserCoupon userCoupon = userCoupons.get(random.nextInt(userCoupons.size()));
                userCouponId = userCoupon.getId();
            }

            OrderItem orderItem = OrderItem.create(
                order.getId(),
                order.getUserId(),
                product.getId(),
                product.getName(),
                quantity,
                product.getPrice(),
                discountAmount,
                userCouponId
            );
            orderItems.add(orderItem);

            // 상품 판매 수량 증가
            product.increaseSalesCount(quantity);
            modifiedProducts.add(product); // 변경된 상품 추적
        }

        // 변경된 상품만 업데이트 (10,000개가 아닌 실제 변경된 것만!)
        productRepository.saveAll(modifiedProducts);
        log.info("  → 실제 업데이트된 Product 수: {} / {}", modifiedProducts.size(), products.size());

        return orderItemRepository.saveAll(orderItems);
    }

    private List<PointTransaction> createDeductionTransactions(
        List<Order> orders,
        List<Point> points,
        List<User> users
    ) {
        List<PointTransaction> transactions = new ArrayList<>();
        Set<Point> modifiedPoints = new HashSet<>(); // 변경된 포인트만 추적

        for (Order order : orders) {
            // 해당 주문의 사용자 포인트 찾기
            Point userPoint = points.stream()
                .filter(p -> p.getUserId().equals(order.getUserId()))
                .findFirst()
                .orElse(null);

            if (userPoint != null && userPoint.getAmount() >= order.getFinalAmount()) {
                // 포인트 차감
                userPoint.deduct(order.getFinalAmount());
                modifiedPoints.add(userPoint); // 변경된 포인트 추적

                // 포인트 사용 트랜잭션 생성
                PointTransaction transaction = PointTransaction.createDeduction(
                    userPoint.getId(),
                    order.getId(),
                    order.getFinalAmount()
                );
                transactions.add(transaction);
            }
        }

        // 변경된 포인트만 업데이트 (10,000개가 아닌 실제 변경된 것만!)
        pointRepository.saveAll(modifiedPoints);
        log.info("  → 실제 업데이트된 Point 수: {} / {}", modifiedPoints.size(), points.size());

        return pointTransactionRepository.saveAll(transactions);
    }
}
