package com.phm.ecommerce.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "장바구니 정보")
public record CartResponse(
    @Schema(description = "장바구니 아이템 목록")
    List<CartItemResponse> items) {

}
