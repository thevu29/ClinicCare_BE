package com.example.cliniccare.dto;

import com.example.cliniccare.interfaces.ScheduleFormGroup;
import com.example.cliniccare.utils.CustomLocalDateDeserializer;
import com.example.cliniccare.utils.CustomLocalTimeDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class ScheduleFormDTO {
    @NotNull(message = "Service ID is required", groups = {ScheduleFormGroup.Create.class, ScheduleFormGroup.AutoCreate.class})
    private UUID serviceId;

    @NotNull(message = "Doctor ID is required", groups = {ScheduleFormGroup.Create.class, ScheduleFormGroup.AutoCreate.class})
    private UUID doctorProfileId;

    @NotNull(message = "Date is required", groups = {ScheduleFormGroup.Create.class})
    @JsonDeserialize(using = CustomLocalDateDeserializer.class)
    private LocalDate date;

    @NotNull(message = "Dates are required", groups = {ScheduleFormGroup.AutoCreate.class})
    private LocalDate[] dates;

    @NotNull(message = "Time is required", groups = {ScheduleFormGroup.Create.class})
    @JsonDeserialize(using = CustomLocalTimeDeserializer.class)
    private LocalTime time;

    @NotNull(message = "Duration is required", groups = {ScheduleFormGroup.Create.class, ScheduleFormGroup.AutoCreate.class})
    @Min(value = 5, message = "Duration must be at least 5")
    private Integer duration;

    @NotNull(message = "Amount is required", groups = {ScheduleFormGroup.AutoCreate.class})
    @Min(value = 1, message = "Amount must be at least 1")
    private int amount;

    private String status;
}
