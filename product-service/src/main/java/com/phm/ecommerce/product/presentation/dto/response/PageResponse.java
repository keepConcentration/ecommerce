package com.phm.ecommerce.product.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "페이지네이션 응답")
public record PageResponse<T>(
    @Schema(description = "컨텐츠 목록") List<T> content,
    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0") int page,
    @Schema(description = "페이지당 항목 수", example = "20") int size,
    @Schema(description = "전체 항목 수", example = "100") long totalElements,
    @Schema(description = "전체 페이지 수", example = "5") int totalPages,
    @Schema(description = "첫 페이지 여부", example = "true") boolean first,
    @Schema(description = "마지막 페이지 여부", example = "false") boolean last) {}
