package com.example.cliniccare.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {
    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (phone == null || phone.isEmpty()) {
            return true;
        }

        return phone.matches("^(\\\\+84|0)(3[2-9]|7[0-9]|8[0-9]|9[0-9]|1[2-9]|5[0-9]|4[0-9])[0-9]{7}$");
    }
}


