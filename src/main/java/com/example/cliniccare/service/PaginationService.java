package com.example.cliniccare.service;

import com.example.cliniccare.dto.PaginationDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class PaginationService {
    public Pageable getPageable(PaginationDTO paginationQuery) {
        Sort sort = Sort.by(Sort.Direction.fromString(paginationQuery.order), paginationQuery.sortBy);
        return PageRequest.of(paginationQuery.page - 1, paginationQuery.size, sort);
    }

    public int getTotalPages(long totalElements, int size) {
        return (int) Math.ceil((double) totalElements / size);
    }
}
