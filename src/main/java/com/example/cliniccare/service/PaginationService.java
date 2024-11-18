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

    public Pageable getDoctorProfilePageable(PaginationDTO paginationQuery) {
        String sortBy = paginationQuery.sortBy;
        if ("name".equals(sortBy) || "email".equals(sortBy) || "phone".equals(sortBy)) {
            sortBy = "user." + sortBy;
        }

        Sort sort = Sort.by(Sort.Direction.fromString(paginationQuery.order), sortBy);

        return PageRequest.of(paginationQuery.page - 1, paginationQuery.size, sort);
    }

    public Pageable getMedicalRecordPageable (PaginationDTO paginationQuery) {
        String sortBy = paginationQuery.sortBy;
        if ("doctorName".equals(sortBy)) {
            sortBy = "doctor.user.name";
        } else if ("serviceName".equals(sortBy)) {
            sortBy = "service.name";
        } else if ("patientName".equals(sortBy)) {
            sortBy = "patient.name";
        }

        Sort sort = Sort.by(Sort.Direction.fromString(paginationQuery.order), sortBy);

        return PageRequest.of(paginationQuery.page - 1, paginationQuery.size, sort);
    }

    public Pageable getFeedbackPageable(PaginationDTO paginationQuery) {
        String sortBy = paginationQuery.sortBy;
        if ("doctorName".equals(sortBy)) {
            sortBy = "doctor.user.name";
        } else if ("serviceName".equals(sortBy)) {
            sortBy = "service.name";
        } else if ("patientName".equals(sortBy)) {
            sortBy = "patient.name";
        }

        Sort sort = Sort.by(Sort.Direction.fromString(paginationQuery.order), sortBy);

        return PageRequest.of(paginationQuery.page - 1, paginationQuery.size, sort);
    }

    public int getTotalPages(long totalElements, int size) {
        return (int) Math.ceil((double) totalElements / size);
    }
}
