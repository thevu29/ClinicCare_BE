package com.example.cliniccare.utils;

import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.validation.Validation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class DateQueryParser<T> {
    private final String query;
    private final String dateFieldName;
    private static final String DATE_FORMAT_ERROR = "Invalid date format (yyyy-MM-dd)";
    private static final String DATE_SEPARATOR = "to";

    public DateQueryParser(String query, String dateFieldName) {
        this.query = query != null ? query.trim() : "";
        this.dateFieldName = dateFieldName;
    }

    public Page<T> getObjects(JpaRepository<T, UUID> repository, Pageable pageable) {
        if (!(repository instanceof JpaSpecificationExecutor)) {
            throw new IllegalArgumentException("Repository must implement JpaSpecificationExecutor");
        }

        JpaSpecificationExecutor<T> specExecutor = (JpaSpecificationExecutor<T>) repository;

        if (query.isEmpty()) {
            return repository.findAll(pageable);
        }

        Specification<T> spec = createDateSpecification();
        return specExecutor.findAll(spec, pageable);
    }

    public Specification<T> createDateSpecification() {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (this.query.contains(DATE_SEPARATOR)) {
                return handleDateRangeSpecification(root, cb);
            }

            if (this.query.startsWith("<")) {
                return handleBeforeDateSpecification(root, cb);
            }

            if (this.query.startsWith(">")) {
                return handleAfterDateSpecification(root, cb);
            }

            return handleExactDateSpecification(root, cb);
        };
    }

    private Predicate handleDateRangeSpecification(Root<T> root, CriteriaBuilder cb) {
        String[] dates = query.split(DATE_SEPARATOR);
        LocalDate startDate = parseAndValidateDate(dates[0].trim());
        LocalDate endDate = parseAndValidateDate(dates[1].trim());

        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date must be before end date");
        }

        if (root.get(dateFieldName).getJavaType() == LocalDateTime.class) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
            return cb.between(root.get(dateFieldName), startDateTime, endDateTime);
        } else {
            return cb.between(root.get(dateFieldName), startDate, endDate);
        }
    }

    private Predicate handleBeforeDateSpecification(Root<T> root, CriteriaBuilder cb) {
        LocalDate date = parseAndValidateDate(query.substring(1));
        return cb.lessThan(root.get(dateFieldName), date);
    }

    private Predicate handleAfterDateSpecification(Root<T> root, CriteriaBuilder cb) {
        LocalDate date = parseAndValidateDate(query.substring(1));
        return cb.greaterThan(root.get(dateFieldName), date);
    }

    private Predicate handleExactDateSpecification(Root<T> root, CriteriaBuilder cb) {
        LocalDate date = parseAndValidateDate(query);

        if (root.get(dateFieldName).getJavaType() == LocalDateTime.class) {
            return cb.equal(cb.function("DATE", LocalDate.class, root.get(dateFieldName)), date);
        } else {
            return cb.equal(root.get(dateFieldName), date);
        }
    }

    private LocalDate parseAndValidateDate(String date) {
        String trimmedDate = date.trim();
        if (Validation.isNotValidDate(trimmedDate)) {
            throw new BadRequestException(DATE_FORMAT_ERROR);
        }
        return LocalDate.parse(trimmedDate);
    }
}