package com.sosd.sosd_backend.dto.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * API 응답을 표준 형식으로 감싸는 클래스
 *
 * 모든 API 응답을 아래와 같은 일관된 형태로 만듭니다:
 * {
 *   "success": true,
 *   "message": "조회 성공",
 *   "data": [...],
 *   "pagination": {...}
 * }
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {  // <T>는 어떤 데이터 타입이든 담을 수 있음

    /**
     * 요청 성공 여부
     * true: 성공, false: 실패
     */
    private boolean success;

    /**
     * 응답 메시지
     * 예: "커밋 조회 성공", "데이터를 찾을 수 없습니다"
     */
    private String message;

    /**
     * 실제 데이터 목록
     * 예: 커밋 리스트, PR 리스트 등
     */
    private List<T> data;

    /**
     * 페이징 정보
     * 데이터가 여러 페이지로 나뉠 때 사용
     */
    private PaginationInfo pagination;

    /**
     * 페이징 정보를 담는 내부 클래스
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        /**
         * 현재 페이지 번호 (0부터 시작)
         * 예: 0 (첫 번째 페이지)
         */
        private Integer currentPage;

        /**
         * 한 페이지당 항목 수
         * 예: 100 (한 페이지에 100개씩)
         */
        private Integer pageSize;

        /**
         * 전체 항목 개수
         * 예: 523 (전체 523개의 커밋)
         */
        private Long totalElements;

        /**
         * 전체 페이지 수
         * 예: 6 (총 6페이지)
         */
        private Integer totalPages;
    }
}
