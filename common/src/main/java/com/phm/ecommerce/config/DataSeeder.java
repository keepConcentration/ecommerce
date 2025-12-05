package com.phm.ecommerce.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Profile("seed")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final ConfigurableApplicationContext applicationContext;
    private final Random random = new Random();

    private static final int BATCH_SIZE = 50000;  // 배치 크기 증가로 성능 향상
    private static final int TOTAL_USERS = 100_000;
    private static final int TOTAL_PRODUCTS = 10_000;
    private static final int TOTAL_COUPONS = 1_000;
    private static final int TOTAL_USER_COUPONS = 500_000;
    private static final int TOTAL_CART_ITEMS = 200_000;
    private static final int TOTAL_ORDERS = 500_000;

    // 메모리에 저장할 데이터 (정합성 유지용)
    private long[] productPrices;
    private long[] pointBalances;
    private List<long[]> pointTransactionBuffer = new ArrayList<>(); // [pointId, orderId, amount]

    @Override
    public void run(String... args) {
        log.info("========================================");
        log.info("Starting data seeding with data integrity...");
        log.info("========================================");

        long startTime = System.currentTimeMillis();

        // Phase 0: 기존 데이터 정리 및 최적화
        truncateAllTables();
        optimizeMySQLForBulkInsert();

        // Phase 1: 기본 데이터 생성
        seedUsers();
        seedProducts();
        seedCoupons();
        seedUserCoupons();
        seedCartItems();

        // Phase 2: Point 초기화 (잔액 0으로 시작)
        initializePoints();

        // Phase 3: Order + OrderItem + PointTransaction 동시 생성 (정합성 유지)
        seedOrdersWithIntegrity();

        // Phase 4: 추가 충전 트랜잭션 생성 (잔액이 양수가 되도록)
        seedChargeTransactions();

        // Phase 5: Point 잔액 업데이트 (트랜잭션 합계로)
        updatePointBalances();

        // Phase 6: MySQL 설정 복원
        restoreMySQLSettings();

        long endTime = System.currentTimeMillis();
        log.info("========================================");
        log.info("Data seeding completed in {} seconds", (endTime - startTime) / 1000);
        log.info("========================================");

        // Shutdown the application context to terminate the CLI
        log.info("Shutting down application...");
        applicationContext.close();
        System.exit(0);
    }

    private void truncateAllTables() {
        log.info("Truncating all tables...");
        long start = System.currentTimeMillis();

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        // 순서 중요: 외래키 참조 순서 역순으로 삭제
        jdbcTemplate.execute("TRUNCATE TABLE point_transactions");
        jdbcTemplate.execute("TRUNCATE TABLE order_items");
        jdbcTemplate.execute("TRUNCATE TABLE orders");
        jdbcTemplate.execute("TRUNCATE TABLE cart_items");
        jdbcTemplate.execute("TRUNCATE TABLE points");
        jdbcTemplate.execute("TRUNCATE TABLE user_coupons");
        jdbcTemplate.execute("TRUNCATE TABLE coupons");
        jdbcTemplate.execute("TRUNCATE TABLE products");
        jdbcTemplate.execute("TRUNCATE TABLE users");

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

        log.info("All tables truncated in {} ms", System.currentTimeMillis() - start);
    }

    private void optimizeMySQLForBulkInsert() {
        log.info("Optimizing MySQL for bulk insert...");

        try {
            // 권한이 필요 없는 세션 레벨 최적화만 적용
            jdbcTemplate.execute("SET SESSION unique_checks = 0");  // 유니크 체크 비활성화
            jdbcTemplate.execute("SET SESSION autocommit = 0");  // 자동 커밋 비활성화
            log.info("MySQL optimization complete");
        } catch (Exception e) {
            log.warn("Some MySQL optimizations failed (non-critical): {}", e.getMessage());
        }
    }

    private void restoreMySQLSettings() {
        log.info("Restoring MySQL settings...");

        try {
            jdbcTemplate.execute("COMMIT");  // 최종 커밋
            jdbcTemplate.execute("SET SESSION unique_checks = 1");
            jdbcTemplate.execute("SET SESSION autocommit = 1");
            log.info("MySQL settings restored");
        } catch (Exception e) {
            log.warn("Failed to restore some MySQL settings: {}", e.getMessage());
        }
    }

    private void seedUsers() {
        log.info("Seeding {} users...", TOTAL_USERS);
        long start = System.currentTimeMillis();

        String sql = "INSERT INTO users (created_at, updated_at) VALUES (NOW(), NOW())";

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) {
            }

            @Override
            public int getBatchSize() {
                return TOTAL_USERS;
            }
        });

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

        log.info("Users seeded in {} ms", System.currentTimeMillis() - start);
    }

    private void seedProducts() {
        log.info("Seeding {} products...", TOTAL_PRODUCTS);
        long start = System.currentTimeMillis();

        productPrices = new long[TOTAL_PRODUCTS + 1];

        String sql = "INSERT INTO products (name, price, quantity, view_count, sales_count, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, NOW(), NOW())";

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        for (int batch = 0; batch < TOTAL_PRODUCTS / BATCH_SIZE + 1; batch++) {
            int batchStart = batch * BATCH_SIZE;
            int batchEnd = Math.min(batchStart + BATCH_SIZE, TOTAL_PRODUCTS);
            int currentBatchSize = batchEnd - batchStart;

            if (currentBatchSize <= 0) break;

            final int finalBatchStart = batchStart;
            final int finalCurrentBatchSize = currentBatchSize;

            jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws java.sql.SQLException {
                    int index = finalBatchStart + i;
                    long viewCount = random.nextLong(10000);
                    long salesCount = random.nextLong(5000);
                    long price = 1000 + random.nextLong(99000);

                    productPrices[index + 1] = price;

                    ps.setString(1, "Product " + index);
                    ps.setLong(2, price);
                    ps.setLong(3, 100 + random.nextLong(900));
                    ps.setLong(4, viewCount);
                    ps.setLong(5, salesCount);
                }

                @Override
                public int getBatchSize() {
                    return finalCurrentBatchSize;
                }
            });
        }

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

        log.info("Products seeded in {} ms", System.currentTimeMillis() - start);
    }

    private void seedCoupons() {
        log.info("Seeding {} coupons...", TOTAL_COUPONS);
        long start = System.currentTimeMillis();

        String sql = "INSERT INTO coupons (name, discount_amount, total_quantity, issued_quantity, valid_days, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, NOW(), NOW())";

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws java.sql.SQLException {
                long discountAmount = (1 + random.nextLong(10)) * 1000;
                long totalQuantity = 100 + random.nextLong(9900);
                long issuedQuantity = random.nextLong(totalQuantity);

                ps.setString(1, "Coupon " + i);
                ps.setLong(2, discountAmount);
                ps.setLong(3, totalQuantity);
                ps.setLong(4, issuedQuantity);
                ps.setInt(5, 7 + random.nextInt(23));
            }

            @Override
            public int getBatchSize() {
                return TOTAL_COUPONS;
            }
        });

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

        log.info("Coupons seeded in {} ms", System.currentTimeMillis() - start);
    }

    private void seedUserCoupons() {
        log.info("Seeding {} user coupons...", TOTAL_USER_COUPONS);
        long start = System.currentTimeMillis();

        String sql = "INSERT IGNORE INTO user_coupons (user_id, coupon_id, issued_at, used_at, expired_at, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, NOW(), NOW())";

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        for (int batch = 0; batch < TOTAL_USER_COUPONS / BATCH_SIZE + 1; batch++) {
            int batchStart = batch * BATCH_SIZE;
            int batchEnd = Math.min(batchStart + BATCH_SIZE, TOTAL_USER_COUPONS);
            int currentBatchSize = batchEnd - batchStart;

            if (currentBatchSize <= 0) break;

            final int finalCurrentBatchSize = currentBatchSize;

            jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws java.sql.SQLException {
                    long userId = 1 + random.nextLong(TOTAL_USERS);
                    long couponId = 1 + random.nextLong(TOTAL_COUPONS);

                    java.sql.Timestamp issuedAt = new java.sql.Timestamp(
                            System.currentTimeMillis() - random.nextLong(30L * 24 * 60 * 60 * 1000));

                    java.sql.Timestamp expiredAt = new java.sql.Timestamp(
                            issuedAt.getTime() + (7L + random.nextInt(23)) * 24 * 60 * 60 * 1000);

                    java.sql.Timestamp usedAt = null;
                    if (random.nextBoolean() && issuedAt.getTime() < System.currentTimeMillis()) {
                        usedAt = new java.sql.Timestamp(
                                issuedAt.getTime() + random.nextLong(System.currentTimeMillis() - issuedAt.getTime()));
                    }

                    ps.setLong(1, userId);
                    ps.setLong(2, couponId);
                    ps.setTimestamp(3, issuedAt);
                    if (usedAt != null) {
                        ps.setTimestamp(4, usedAt);
                    } else {
                        ps.setNull(4, java.sql.Types.TIMESTAMP);
                    }
                    ps.setTimestamp(5, expiredAt);
                }

                @Override
                public int getBatchSize() {
                    return finalCurrentBatchSize;
                }
            });

            if ((batch + 1) % 10 == 0) {
                log.info("User coupons progress: {}/{}", Math.min((batch + 1) * BATCH_SIZE, TOTAL_USER_COUPONS), TOTAL_USER_COUPONS);
            }
        }

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

        log.info("User coupons seeded in {} ms", System.currentTimeMillis() - start);
    }

    private void seedCartItems() {
        log.info("Seeding {} cart items...", TOTAL_CART_ITEMS);
        long start = System.currentTimeMillis();

        String sql = "INSERT IGNORE INTO cart_items (user_id, product_id, quantity, created_at, updated_at) " +
                "VALUES (?, ?, ?, NOW(), NOW())";

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        for (int batch = 0; batch < TOTAL_CART_ITEMS / BATCH_SIZE + 1; batch++) {
            int batchStart = batch * BATCH_SIZE;
            int batchEnd = Math.min(batchStart + BATCH_SIZE, TOTAL_CART_ITEMS);
            int currentBatchSize = batchEnd - batchStart;

            if (currentBatchSize <= 0) break;

            final int finalCurrentBatchSize = currentBatchSize;

            jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws java.sql.SQLException {
                    long userId = 1 + random.nextLong(TOTAL_USERS);
                    long productId = 1 + random.nextLong(TOTAL_PRODUCTS);
                    long quantity = 1 + random.nextLong(10);

                    ps.setLong(1, userId);
                    ps.setLong(2, productId);
                    ps.setLong(3, quantity);
                }

                @Override
                public int getBatchSize() {
                    return finalCurrentBatchSize;
                }
            });

            if ((batch + 1) % 10 == 0) {
                log.info("Cart items progress: {}/{}", Math.min((batch + 1) * BATCH_SIZE, TOTAL_CART_ITEMS), TOTAL_CART_ITEMS);
            }
        }

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

        log.info("Cart items seeded in {} ms", System.currentTimeMillis() - start);
    }

    private void initializePoints() {
        log.info("Initializing {} points with zero balance...", TOTAL_USERS);
        long start = System.currentTimeMillis();

        pointBalances = new long[TOTAL_USERS + 1];

        String sql = "INSERT IGNORE INTO points (user_id, amount, created_at, updated_at) " +
                "VALUES (?, 0, NOW(), NOW())";

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        for (int batch = 0; batch < TOTAL_USERS / BATCH_SIZE + 1; batch++) {
            int batchStart = batch * BATCH_SIZE;
            int batchEnd = Math.min(batchStart + BATCH_SIZE, TOTAL_USERS);
            int currentBatchSize = batchEnd - batchStart;

            if (currentBatchSize <= 0) break;

            final int finalBatchStart = batchStart;
            final int finalCurrentBatchSize = currentBatchSize;

            jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws java.sql.SQLException {
                    long userId = finalBatchStart + i + 1;
                    ps.setLong(1, userId);
                }

                @Override
                public int getBatchSize() {
                    return finalCurrentBatchSize;
                }
            });
        }

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

        log.info("Points initialized in {} ms", System.currentTimeMillis() - start);
    }

    private void seedOrdersWithIntegrity() {
        log.info("Seeding {} orders with integrity (Order + OrderItems + PointTransactions)...", TOTAL_ORDERS);
        long start = System.currentTimeMillis();

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        for (int batch = 0; batch < TOTAL_ORDERS / BATCH_SIZE + 1; batch++) {
            int batchStart = batch * BATCH_SIZE;
            int batchEnd = Math.min(batchStart + BATCH_SIZE, TOTAL_ORDERS);
            int currentBatchSize = batchEnd - batchStart;

            if (currentBatchSize <= 0) break;

            List<OrderData> orderDataList = new ArrayList<>();

            // 1. 주문 데이터 생성 (메모리)
            for (int i = 0; i < currentBatchSize; i++) {
                long orderId = batchStart + i + 1;
                long userId = 1 + random.nextLong(TOTAL_USERS);
                int itemCount = 1 + random.nextInt(3); // 1~3개 아이템

                List<OrderItemData> items = new ArrayList<>();
                long totalAmount = 0;
                long totalDiscount = 0;

                for (int j = 0; j < itemCount; j++) {
                    long productId = 1 + random.nextLong(TOTAL_PRODUCTS);
                    long price = productPrices[(int) productId];
                    long quantity = 1 + random.nextLong(3);
                    long itemTotalPrice = price * quantity;
                    long itemDiscount = random.nextLong(Math.max(1, itemTotalPrice / 10));
                    long itemFinalAmount = itemTotalPrice - itemDiscount;

                    items.add(new OrderItemData(orderId, userId, productId, "Product " + productId,
                            quantity, price, itemTotalPrice, itemDiscount, itemFinalAmount));

                    totalAmount += itemTotalPrice;
                    totalDiscount += itemDiscount;
                }

                long finalAmount = totalAmount - totalDiscount;
                orderDataList.add(new OrderData(orderId, userId, totalAmount, totalDiscount, finalAmount, items));

                // PointTransaction 버퍼에 추가 (차감)
                long pointId = userId; // pointId = userId (1:1 관계)
                pointTransactionBuffer.add(new long[]{pointId, orderId, -finalAmount});
                pointBalances[(int) userId] -= finalAmount;
            }

            // 2. Orders 배치 삽입
            String orderSql = "INSERT INTO orders (user_id, total_amount, discount_amount, final_amount, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, NOW(), NOW())";

            final List<OrderData> finalOrderDataList = orderDataList;
            jdbcTemplate.batchUpdate(orderSql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws java.sql.SQLException {
                    OrderData order = finalOrderDataList.get(i);
                    ps.setLong(1, order.userId);
                    ps.setLong(2, order.totalAmount);
                    ps.setLong(3, order.discountAmount);
                    ps.setLong(4, order.finalAmount);
                }

                @Override
                public int getBatchSize() {
                    return finalOrderDataList.size();
                }
            });

            // 3. OrderItems 배치 삽입
            List<OrderItemData> allItems = new ArrayList<>();
            for (OrderData order : orderDataList) {
                allItems.addAll(order.items);
            }

            if (!allItems.isEmpty()) {
                String itemSql = "INSERT INTO order_items (order_id, user_id, product_id, user_coupon_id, product_name, quantity, price, total_price, discount_amount, final_amount, created_at, updated_at) " +
                        "VALUES (?, ?, ?, NULL, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

                final List<OrderItemData> finalAllItems = allItems;
                jdbcTemplate.batchUpdate(itemSql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws java.sql.SQLException {
                        OrderItemData item = finalAllItems.get(i);
                        ps.setLong(1, item.orderId);
                        ps.setLong(2, item.userId);
                        ps.setLong(3, item.productId);
                        ps.setString(4, item.productName);
                        ps.setLong(5, item.quantity);
                        ps.setLong(6, item.price);
                        ps.setLong(7, item.totalPrice);
                        ps.setLong(8, item.discountAmount);
                        ps.setLong(9, item.finalAmount);
                    }

                    @Override
                    public int getBatchSize() {
                        return finalAllItems.size();
                    }
                });
            }

            if ((batch + 1) % 10 == 0) {
                log.info("Orders progress: {}/{}", Math.min((batch + 1) * BATCH_SIZE, TOTAL_ORDERS), TOTAL_ORDERS);
            }
        }

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

        log.info("Orders with integrity seeded in {} ms", System.currentTimeMillis() - start);
    }

    private void seedChargeTransactions() {
        log.info("Seeding charge transactions to ensure positive balances...");
        long start = System.currentTimeMillis();

        // 각 유저의 잔액이 양수가 되도록 충전 트랜잭션 추가
        for (int userId = 1; userId <= TOTAL_USERS; userId++) {
            long currentBalance = pointBalances[userId];
            if (currentBalance < 0) {
                // 음수 잔액을 커버하고 추가 여유분 (10,000 ~ 100,000) 충전
                long chargeAmount = Math.abs(currentBalance) + 10000 + random.nextLong(90000);
                pointTransactionBuffer.add(new long[]{userId, 0, chargeAmount}); // orderId=0 means null
                pointBalances[userId] += chargeAmount;
            }
            // 추가로 50% 확률로 여유 충전
            if (random.nextBoolean()) {
                long extraCharge = 10000 + random.nextLong(90000);
                pointTransactionBuffer.add(new long[]{userId, 0, extraCharge});
                pointBalances[userId] += extraCharge;
            }
        }

        // PointTransactions 배치 삽입
        log.info("Inserting {} point transactions...", pointTransactionBuffer.size());

        String sql = "INSERT INTO point_transactions (point_id, order_id, amount, created_at) VALUES (?, ?, ?, ?)";

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        for (int batch = 0; batch < pointTransactionBuffer.size() / BATCH_SIZE + 1; batch++) {
            int batchStart = batch * BATCH_SIZE;
            int batchEnd = Math.min(batchStart + BATCH_SIZE, pointTransactionBuffer.size());
            int currentBatchSize = batchEnd - batchStart;

            if (currentBatchSize <= 0) break;

            final int finalBatchStart = batchStart;
            final int finalCurrentBatchSize = currentBatchSize;

            jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws java.sql.SQLException {
                    long[] tx = pointTransactionBuffer.get(finalBatchStart + i);
                    long pointId = tx[0];
                    long orderId = tx[1];
                    long amount = tx[2];

                    java.sql.Timestamp createdAt = new java.sql.Timestamp(
                            System.currentTimeMillis() - random.nextLong(90L * 24 * 60 * 60 * 1000));

                    ps.setLong(1, pointId);
                    if (orderId > 0) {
                        ps.setLong(2, orderId);
                    } else {
                        ps.setNull(2, java.sql.Types.BIGINT);
                    }
                    ps.setLong(3, amount);
                    ps.setTimestamp(4, createdAt);
                }

                @Override
                public int getBatchSize() {
                    return finalCurrentBatchSize;
                }
            });

            if ((batch + 1) % 50 == 0) {
                log.info("Point transactions progress: {}/{}", Math.min((batch + 1) * BATCH_SIZE, pointTransactionBuffer.size()), pointTransactionBuffer.size());
            }
        }

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

        log.info("Charge transactions seeded in {} ms", System.currentTimeMillis() - start);
    }

    private void updatePointBalances() {
        log.info("Updating point balances from transaction sums...");
        long start = System.currentTimeMillis();

        // Point 잔액을 PointTransaction 합계로 업데이트
        String updateSql = "UPDATE points p SET p.amount = " +
                "(SELECT COALESCE(SUM(pt.amount), 0) FROM point_transactions pt WHERE pt.point_id = p.id)";

        jdbcTemplate.execute(updateSql);

        // 정합성 검증
        Long negativeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM points WHERE amount < 0", Long.class);

        if (negativeCount != null && negativeCount > 0) {
            log.warn("Found {} points with negative balance!", negativeCount);
        } else {
            log.info("All point balances are non-negative. Data integrity verified.");
        }

        log.info("Point balances updated in {} ms", System.currentTimeMillis() - start);
    }

    // 내부 데이터 클래스
    private static class OrderData {
        long orderId;
        long userId;
        long totalAmount;
        long discountAmount;
        long finalAmount;
        List<OrderItemData> items;

        OrderData(long orderId, long userId, long totalAmount, long discountAmount, long finalAmount, List<OrderItemData> items) {
            this.orderId = orderId;
            this.userId = userId;
            this.totalAmount = totalAmount;
            this.discountAmount = discountAmount;
            this.finalAmount = finalAmount;
            this.items = items;
        }
    }

    private static class OrderItemData {
        long orderId;
        long userId;
        long productId;
        String productName;
        long quantity;
        long price;
        long totalPrice;
        long discountAmount;
        long finalAmount;

        OrderItemData(long orderId, long userId, long productId, String productName,
                      long quantity, long price, long totalPrice, long discountAmount, long finalAmount) {
            this.orderId = orderId;
            this.userId = userId;
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.price = price;
            this.totalPrice = totalPrice;
            this.discountAmount = discountAmount;
            this.finalAmount = finalAmount;
        }
    }
}
