package com.phm.ecommerce.product.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;

@Schema(description = "인기 상품 요청")
public record PopularProductRequest(

    @Schema(description = "인기 상품 조회 개수", example = "10")
    @Positive(message = "0 이상의 숫자를 입력해주세요.")
    @Max(value = 100, message = "100개 이하의 상품만 조회가 가능합니다.")
    Integer limit
) {

}
