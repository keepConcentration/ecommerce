package com.phm.ecommerce.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "포인트 충전 요청")
public class ChargePointsRequest {

  @NotNull(message = "사용자 ID는 필수입니다")
  @Schema(description = "사용자 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long userId;

  @NotNull(message = "충전 금액은 필수입니다")
  @Min(value = 1, message = "충전 금액은 1 이상이어야 합니다")
  @Schema(
      description = "충전 금액 (1 이상)",
      example = "100000",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long amount;
}
