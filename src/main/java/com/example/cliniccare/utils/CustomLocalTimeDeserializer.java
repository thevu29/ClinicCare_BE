package com.example.cliniccare.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Set;

public class CustomLocalTimeDeserializer extends StdDeserializer<LocalTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final Set<Integer> VALID_MINUTES = Set.of(0, 5, 15, 30);

    public CustomLocalTimeDeserializer() {
        super(LocalTime.class);
    }

    @Override
    public LocalTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String timeStr = parser.getText().trim();
        try {
            LocalTime time = LocalTime.parse(timeStr, FORMATTER);

            if (!VALID_MINUTES.contains(time.getMinute())) {
                throw new com.fasterxml.jackson.databind.exc.InvalidFormatException(
                        parser,
                        "Invalid time. Minutes must be 00, 05, 15, or 30",
                        timeStr,
                        LocalTime.class
                );
            }

            return time;
        } catch (DateTimeParseException e) {
            throw new com.fasterxml.jackson.databind.exc.InvalidFormatException(
                    parser,
                    "Time must be in format HH:mm (e.g., 09:30, 14:45)",
                    timeStr,
                    LocalTime.class
            );
        }
    }
}

