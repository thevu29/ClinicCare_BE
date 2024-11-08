package com.example.cliniccare.utils;

import com.example.cliniccare.exception.BadRequestException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class PriceQueryParser<T> {
    private final String query;
    private final String priceFieldName;
    private static final String PRICE_ERROR = "Price must be a number or range (e.g. 1000, >1000, <1000 or 1000to2000)";
    private static final String PRICE_SEPARATOR = "to";

    public PriceQueryParser(String query, String priceFieldName) {
        this.query = query != null ? query.trim() : "";
        this.priceFieldName = priceFieldName;
    }

    public Specification<T> createPriceSpecification() {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (this.query.contains(PRICE_SEPARATOR)) {
                return handlePriceRangeSpecification(root, cb);
            }

            if (this.query.startsWith("<")) {
                return handleLessThanPriceSpecification(root, cb);
            }

            if (this.query.startsWith(">")) {
                return handleGreaterThanPriceSpecification(root, cb);
            }

            return handleExactPriceSpecification(root, cb);
        };
    }

    private Predicate handlePriceRangeSpecification(Root<T> root, CriteriaBuilder cb) {
        String[] prices = this.query.split(PRICE_SEPARATOR);

        if (prices.length != 2) {
            throw new BadRequestException(PRICE_ERROR);
        }

        try {
            int minPrice = Integer.parseInt(prices[0]);
            int maxPrice = Integer.parseInt(prices[1]);

            if (minPrice > maxPrice) {
                throw new BadRequestException(PRICE_ERROR);
            }

            return cb.between(root.get(priceFieldName), minPrice, maxPrice);
        } catch (NumberFormatException e) {
            throw new BadRequestException(PRICE_ERROR);
        }
    }

    private Predicate handleGreaterThanPriceSpecification(Root<T> root, CriteriaBuilder cb) {
        try {
            int price = Integer.parseInt(this.query.substring(1));
            return cb.greaterThanOrEqualTo(root.get(priceFieldName), price);
        } catch (NumberFormatException e) {
            throw new BadRequestException(PRICE_ERROR);
        }
    }

    private Predicate handleLessThanPriceSpecification(Root<T> root, CriteriaBuilder cb) {
        try {
            int price = Integer.parseInt(this.query.substring(1));
            return cb.lessThanOrEqualTo(root.get(priceFieldName), price);
        } catch (NumberFormatException e) {
            throw new BadRequestException(PRICE_ERROR);
        }
    }

    private Predicate handleExactPriceSpecification(Root<T> root, CriteriaBuilder cb) {
        try {
            int price = Integer.parseInt(this.query);
            return cb.equal(root.get(priceFieldName), price);
        } catch (NumberFormatException e) {
            throw new BadRequestException(PRICE_ERROR);
        }
    }
}
