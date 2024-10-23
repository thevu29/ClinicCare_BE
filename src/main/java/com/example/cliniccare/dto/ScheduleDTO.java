package com.example.cliniccare.dto;

import com.example.cliniccare.model.Schedule;
import com.example.cliniccare.utils.Formatter;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class ScheduleDTO {
    private UUID serviceId;
    private String serviceName;
    private UUID doctorProfileId;
    private String doctorName;
    private LocalDate date;
    private List<ScheduleDetailDTO> scheduleDetails;

    public ScheduleDTO() {
    }

    public ScheduleDTO(List<Schedule> schedules) {
        if (!schedules.isEmpty()) {
            Schedule firstSchedule = schedules.getFirst();

            this.serviceId = firstSchedule.getService().getServiceId();
            this.serviceName = firstSchedule.getService().getName();
            this.doctorName = firstSchedule.getDoctor().getUser().getName();
            this.doctorProfileId = firstSchedule.getDoctor().getDoctorProfileId();
            this.date = firstSchedule.getDateTime().toLocalDate();

            this.scheduleDetails = schedules.stream()
                    .map(schedule -> new ScheduleDetailDTO(
                            schedule.getScheduleId(),
                            Formatter.formatTime(schedule.getDateTime()),
                            schedule.getDuration(),
                            schedule.getStatus().name()
                    ))
                    .collect(Collectors.toList());
        }
    }

    public ScheduleDTO(Schedule schedule) {
        this.serviceId = schedule.getService().getServiceId();
        this.serviceName = schedule.getService().getName();
        this.doctorName = schedule.getDoctor().getUser().getName();
        this.doctorProfileId = schedule.getDoctor().getDoctorProfileId();
        this.date = schedule.getDateTime().toLocalDate();
        this.scheduleDetails = List.of(new ScheduleDetailDTO(
                schedule.getScheduleId(),
                Formatter.formatTime(schedule.getDateTime()),
                schedule.getDuration(),
                schedule.getStatus().name()
        ));
    }
}
