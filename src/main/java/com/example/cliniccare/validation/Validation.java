package com.example.cliniccare.validation;

import com.example.cliniccare.response.ApiResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Validation {
    public static ResponseEntity<?> validateBody(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", "));

            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false, errors, null
            ));
        }
        return null;
    }

    public static boolean isNotValidDate(String date) {
        String datePattern = "^\\d{4}-\\d{2}-\\d{2}$";
        if (!Pattern.matches(datePattern, date)) {
            return true;
        }

        try {
            LocalDate.parse(date);
            return false;
        } catch (DateTimeParseException e) {
            return true;
        }
    }
}
