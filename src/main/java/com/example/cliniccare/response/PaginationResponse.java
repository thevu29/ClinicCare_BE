package com.example.cliniccare.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaginationResponse<T> extends ApiResponse<T> {
    private Meta meta;

    public PaginationResponse(
            boolean success,
            String message,
            T data,
            int page,
            int size,
            int take,
            int totalPages,
            long totalElements
    ) {
        super(success, message, data);
        this.meta = new Meta(page, size, take, totalPages, totalElements);
    }

    @Setter
    @Getter
    private static class Meta {
        private int page;
        private int size;
        private int take;
        private int totalPages;
        private long totalElements;

        public Meta(int page, int size, int take, int totalPages, long totalElements) {
            this.page = page;
            this.size = size;
            this.take = take;
            this.totalPages = totalPages;
            this.totalElements = totalElements;
        }
    }
}