package com.example.cliniccare.dto;

import com.example.cliniccare.entity.Schedule;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ScheduleDTO {
    private UUID scheduleId;
    private UUID serviceId;
    private String serviceName;
    private UUID doctorProfileId;
    private String doctorName;
    private LocalDateTime dateTime;
    private String status;
    private int duration;

    public ScheduleDTO() {}

    public ScheduleDTO(Schedule schedule) {
        this.scheduleId = schedule.getScheduleId();
        this.serviceId = schedule.getService().getServiceId();
        this.serviceName = schedule.getService().getName();
        this.doctorName = schedule.getDoctor().getUser().getName();
        this.doctorProfileId = schedule.getDoctor().getDoctorProfileId();
        this.dateTime = schedule.getDateTime();
        this.status = schedule.getStatus().name();
        this.duration = schedule.getDuration();
    }
}
