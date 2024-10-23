package com.example.cliniccare.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MinArrayValidator implements ConstraintValidator<MinArray, Integer[]> {
    private int minValue;

    @Override
    public void initialize(MinArray constraintAnnotation) {
        this.minValue = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Integer[] value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        for (Integer i : value) {
            if (i == null || i < minValue) {
                return false;
            }
        }
        return true;
    }
}