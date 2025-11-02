package com.phm.ecommerce.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "수량 변경 요청")
public record UpdateQuantityRequest(
    @Schema(description = "변경할 수량 (1 이상)", example = "5", requiredMode = RequiredMode.REQUIRED)
    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1 이상이어야 합니다")
    Long quantity) {

}
