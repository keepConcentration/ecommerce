package com.phm.ecommerce.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "에러 상세 정보")
public class ErrorDetail {

  @Schema(description = "에러 코드", example = "PRODUCT_NOT_FOUND")
  private String code;

  @Schema(description = "에러 메시지", example = "상품이 존재하지 않습니다")
  private String message;
}
