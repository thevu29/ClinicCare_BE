package com.example.cliniccare.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotEmptyIfPresentValidator implements ConstraintValidator<NotEmptyIfPresent, String> {

    @Override
    public void initialize(NotEmptyIfPresent constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // If the field is present (not null), it must not be empty
        return value == null || !value.trim().isEmpty();
    }
}