package com.phm.ecommerce.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 쿠폰 정보")
public class UserCouponResponse {

  @Schema(description = "사용자 쿠폰 ID", example = "10")
  private Long userCouponId;

  @Schema(description = "사용자 ID", example = "1")
  private Long userId;

  @Schema(description = "쿠폰 ID", example = "1")
  private Long couponId;

  @Schema(description = "쿠폰명", example = "신규 가입 50000원 할인")
  private String couponName;

  @Schema(description = "할인 금액", example = "50000")
  private Long discountAmount;

  @Schema(description = "발급일시", example = "2025-01-20T10:00:00")
  private LocalDateTime issuedAt;

  @Schema(description = "만료일시", example = "2025-01-27T23:59:59")
  private LocalDateTime expiredAt;
}
