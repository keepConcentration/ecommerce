package com.phm.ecommerce.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "수량 변경 요청")
public class UpdateQuantityRequest {

  @NotNull(message = "수량은 필수입니다")
  @Min(value = 1, message = "수량은 1 이상이어야 합니다")
  @Schema(description = "변경할 수량 (1 이상)", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long quantity;
}
