package com.example.cliniccare.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ScheduleDetailDTO {
    private UUID scheduleId;
    private String time;
    private int duration;
    private String status;

    public ScheduleDetailDTO(UUID scheduleId, String time, int duration, String status) {
        this.scheduleId = scheduleId;
        this.time = time;
        this.duration = duration;
        this.status = status;
    }
}
