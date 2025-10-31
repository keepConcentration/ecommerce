package com.phm.ecommerce.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "포인트 정보")
public class PointResponse {

  @Schema(description = "포인트 ID", example = "1")
  private Long pointId;

  @Schema(description = "사용자 ID", example = "1")
  private Long userId;

  @Schema(description = "포인트 잔액", example = "50000")
  private Long amount;

  @Schema(description = "수정일시", example = "2025-01-20T10:00:00")
  private LocalDateTime updatedAt;
}
