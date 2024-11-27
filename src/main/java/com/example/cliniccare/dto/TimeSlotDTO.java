package com.example.cliniccare.dto;

import lombok.Data;

import java.time.LocalTime;

@Data
public class TimeSlotDTO {
    private LocalTime start;
    private LocalTime end;

    public TimeSlotDTO() {}

    public TimeSlotDTO(LocalTime start, LocalTime end) {
        this.start = start;
        this.end = end;
    }
}
