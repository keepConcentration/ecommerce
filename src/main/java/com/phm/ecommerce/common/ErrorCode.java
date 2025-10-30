package com.phm.ecommerce.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "상품이 존재하지 않습니다", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_FOUND("CART_ITEM_NOT_FOUND", "장바구니 아이템이 존재하지 않습니다", HttpStatus.NOT_FOUND),
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "주문이 존재하지 않습니다", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("USER_NOT_FOUND", "사용자가 존재하지 않습니다", HttpStatus.NOT_FOUND),
    COUPON_NOT_FOUND("COUPON_NOT_FOUND", "쿠폰을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    INSUFFICIENT_STOCK("INSUFFICIENT_STOCK", "재고가 부족합니다", HttpStatus.CONFLICT),
    INSUFFICIENT_POINTS("INSUFFICIENT_POINTS", "포인트가 부족합니다", HttpStatus.CONFLICT),
    COUPON_SOLD_OUT("COUPON_SOLD_OUT", "쿠폰이 모두 소진되었습니다", HttpStatus.CONFLICT),
    COUPON_ALREADY_ISSUED("COUPON_ALREADY_ISSUED", "이미 발급받은 쿠폰입니다", HttpStatus.CONFLICT),
    COUPON_EXPIRED("COUPON_EXPIRED", "만료된 쿠폰입니다", HttpStatus.BAD_REQUEST),
    COUPON_ALREADY_USED("COUPON_ALREADY_USED", "이미 사용된 쿠폰입니다", HttpStatus.BAD_REQUEST),
    INVALID_AMOUNT("INVALID_AMOUNT", "유효하지 않은 금액입니다", HttpStatus.BAD_REQUEST),
    INVALID_QUANTITY("INVALID_QUANTITY", "유효하지 않은 수량입니다", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST("INVALID_REQUEST", "잘못된 요청입니다", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "내부 서버 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
