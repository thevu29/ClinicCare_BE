package com.example.cliniccare.dto;

import com.example.cliniccare.interfaces.ScheduleFormGroup;
import com.example.cliniccare.validation.MinArray;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ScheduleFormDTO {
    @NotNull(message = "Service ID is required", groups = {ScheduleFormGroup.Create.class})
    private UUID serviceId;

    @NotNull(message = "Doctor ID is required", groups = {ScheduleFormGroup.Create.class})
    private UUID doctorProfileId;

    @NotNull(message = "Date is required", groups = {ScheduleFormGroup.Create.class})
    private LocalDate date;

    @NotNull(message = "Times are required", groups = {ScheduleFormGroup.Create.class})
    private String[] times;

    private String time;

    @Min(value = 5, message = "Duration must be at least 5")
    private Integer duration;

    @NotNull(message = "Durations are required", groups = {ScheduleFormGroup.Create.class})
    @MinArray(value = 5, message = "Duration must be at least 5", groups = {ScheduleFormGroup.Create.class})
    private Integer[] durations;

    private String status;
}
