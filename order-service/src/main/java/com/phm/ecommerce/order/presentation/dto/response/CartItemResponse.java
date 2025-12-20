package com.phm.ecommerce.order.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "장바구니 아이템 정보")
public record CartItemResponse(
    @Schema(description = "장바구니 아이템 ID", example = "1")
    Long cartItemId,

    @Schema(description = "상품 ID", example = "1")
    Long productId,

    @Schema(description = "상품명", example = "노트북")
    String productName,

    @Schema(description = "가격", example = "1500000")
    Long price,

    @Schema(description = "수량", example = "3")
    Long quantity) {

}
