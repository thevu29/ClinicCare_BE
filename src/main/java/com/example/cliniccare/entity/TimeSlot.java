package com.example.cliniccare.entity;

import lombok.Data;

import java.time.LocalTime;

@Data
public class TimeSlot {
    private LocalTime start;
    private LocalTime end;

    public TimeSlot() {}

    public TimeSlot(LocalTime start, LocalTime end) {
        this.start = start;
        this.end = end;
    }
}
