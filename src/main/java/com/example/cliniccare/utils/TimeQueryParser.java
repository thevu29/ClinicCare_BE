package com.example.cliniccare.utils;

import com.example.cliniccare.exception.BadRequestException;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TimeQueryParser<T> {
    private final String query;
    private final String timeFieldName;
    private static final String TIME_FORMAT_ERROR = "Invalid time format (HH:mm)";
    private static final String TIME_SEPARATOR = "to";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public TimeQueryParser(String query, String timeFieldName) {
        this.query = query != null ? query.trim() : "";
        this.timeFieldName = timeFieldName;
    }

    public Specification<T> createTimeSpecification() {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (this.query.contains(TIME_SEPARATOR)) {
                return handleTimeRangeSpecification(root, cb);
            }

            if (this.query.startsWith("<")) {
                return handleBeforeTimeSpecification(root, cb);
            }

            if (this.query.startsWith(">")) {
                return handleAfterTimeSpecification(root, cb);
            }

            return handleExactTimeSpecification(root, cb);
        };
    }

    private Predicate handleTimeRangeSpecification(Root<T> root, CriteriaBuilder cb) {
        String[] times = this.query.split(TIME_SEPARATOR);

        if (times.length != 2) {
            throw new BadRequestException(TIME_FORMAT_ERROR);
        }

        LocalTime start = parseTime(times[0]);
        LocalTime end = parseTime(times[1]);

        if (start.isAfter(end)) {
            throw new BadRequestException("Start time must be before end time");
        }

        Expression<Integer> hourExpression = cb.function("HOUR", Integer.class, root.get(this.timeFieldName));
        Expression<Integer> minuteExpression = cb.function("MINUTE", Integer.class, root.get(this.timeFieldName));

        Predicate startTimePredicate = cb.or(
                cb.greaterThan(hourExpression, start.getHour()),
                cb.and(
                        cb.equal(hourExpression, start.getHour()),
                        cb.greaterThanOrEqualTo(minuteExpression, start.getMinute())
                )
        );

        Predicate endTimePredicate = cb.or(
                cb.lessThan(hourExpression, end.getHour()),
                cb.and(
                        cb.equal(hourExpression, end.getHour()),
                        cb.lessThanOrEqualTo(minuteExpression, end.getMinute())
                )
        );

        return cb.and(startTimePredicate, endTimePredicate);
    }

    private Predicate handleBeforeTimeSpecification(Root<T> root, CriteriaBuilder cb) {
        String time = this.query.substring(1);
        LocalTime targetTime = parseTime(time);

        Expression<Integer> hourExpression = cb.function("HOUR", Integer.class, root.get(this.timeFieldName));
        Expression<Integer> minuteExpression = cb.function("MINUTE", Integer.class, root.get(this.timeFieldName));

        return cb.or(
                cb.lessThan(hourExpression, targetTime.getHour()),
                cb.and(
                        cb.equal(hourExpression, targetTime.getHour()),
                        cb.lessThan(minuteExpression, targetTime.getMinute())
                )
        );
    }

    private Predicate handleAfterTimeSpecification(Root<T> root, CriteriaBuilder cb) {
        String time = this.query.substring(1);
        LocalTime targetTime = parseTime(time);

        Expression<Integer> hourExpression = cb.function("HOUR", Integer.class, root.get(this.timeFieldName));
        Expression<Integer> minuteExpression = cb.function("MINUTE", Integer.class, root.get(this.timeFieldName));

        return cb.or(
                cb.greaterThan(hourExpression, targetTime.getHour()),
                cb.and(
                        cb.equal(hourExpression, targetTime.getHour()),
                        cb.greaterThan(minuteExpression, targetTime.getMinute())
                )
        );
    }

    private Predicate handleExactTimeSpecification(Root<T> root, CriteriaBuilder cb) {
        LocalTime targetTime = parseTime(this.query);

        return cb.and(
                cb.equal(cb.function("HOUR", Integer.class, root.get(this.timeFieldName)), targetTime.getHour()),
                cb.equal(cb.function("MINUTE", Integer.class, root.get(this.timeFieldName)), targetTime.getMinute())
        );
    }

    private LocalTime parseTime(String timeString) {
        try {
            return LocalTime.parse(timeString, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new BadRequestException(TIME_FORMAT_ERROR);
        }
    }
}