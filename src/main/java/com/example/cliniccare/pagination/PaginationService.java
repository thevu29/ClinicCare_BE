package com.example.cliniccare.pagination;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class PaginationService {
    public Pageable getPageable(PaginationQuery paginationQuery) {
        Sort sort = Sort.by(Sort.Direction.fromString(paginationQuery.order), paginationQuery.sortBy);
        return PageRequest.of(paginationQuery.page - 1, paginationQuery.size, sort);
    }

    public int getTotalPages(long totalElements, int size) {
        return (int) Math.ceil((double) totalElements / size);
    }

    public int getOffset(int page, int size) {
        return (page - 1) * size;
    }
}
