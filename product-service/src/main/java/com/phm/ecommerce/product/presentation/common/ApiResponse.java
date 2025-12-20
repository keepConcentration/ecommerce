package com.phm.ecommerce.product.presentation.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "API 공통 응답")
public class ApiResponse<T> {

  @Schema(description = "성공 여부", example = "true")
  private boolean status;

  @Schema(description = "응답 데이터")
  private T data;

  @Schema(description = "에러 정보")
  private ErrorDetail error;

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, data, null);
  }

  public static <T> ApiResponse<T> error(String code, String message) {
    return new ApiResponse<>(false, null, new ErrorDetail(code, message));
  }

  public static <T> ApiResponse<T> error(ErrorDetail errorDetail) {
    return new ApiResponse<>(false, null, errorDetail);
  }
}
