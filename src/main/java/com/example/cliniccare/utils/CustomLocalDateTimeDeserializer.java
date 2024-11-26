package com.example.cliniccare.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Set;

public class CustomLocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Set<Integer> VALID_MINUTES = Set.of(0, 5, 15, 30);

    public CustomLocalDateTimeDeserializer() {
        super(LocalDateTime.class);
    }

    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String dateTimeStr = parser.getText().trim();
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, FORMATTER);

            if (!VALID_MINUTES.contains(dateTime.getMinute())) {
                throw new com.fasterxml.jackson.databind.exc.InvalidFormatException(
                        parser,
                        "Invalid time. Minutes must be 00, 05, 15, or 30",
                        dateTimeStr,
                        LocalDateTime.class
                );
            }

            return dateTime;
        } catch (DateTimeParseException e) {
            throw new com.fasterxml.jackson.databind.exc.InvalidFormatException(
                    parser,
                    "DateTime must be in format yyyy-MM-dd HH:mm (e.g., 2024-10-15 14:30)",
                    dateTimeStr,
                    LocalDateTime.class
            );
        }
    }
}