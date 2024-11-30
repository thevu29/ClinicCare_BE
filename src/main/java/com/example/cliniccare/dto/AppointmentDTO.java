package com.example.cliniccare.dto;

import com.example.cliniccare.interfaces.AppointmentGroup;
import com.example.cliniccare.entity.Appointment;
import com.example.cliniccare.validation.ValidPhone;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AppointmentDTO {
    private UUID appointmentId;

    @NotBlank(message = "Patient name is required", groups = {AppointmentGroup.Create.class})
    private String patientName;

    @NotBlank(message = "Patient phone is required", groups = {AppointmentGroup.Create.class})
    @ValidPhone(message = "Invalid phone number", groups = {AppointmentGroup.Create.class})
    private String patientPhone;

    @NotNull(message = "Schedule ID is required", groups = {AppointmentGroup.Create.class})
    private UUID scheduleId;

    @NotNull(message = "Patient ID is required", groups = {AppointmentGroup.Create.class})
    private UUID patientId;

    private LocalDateTime date;

    @NotNull(message = "Cancel by is required", groups = {AppointmentGroup.Cancel.class})
    private UUID cancelBy;

    private LocalDateTime cancelAt;

    @NotBlank(message = "Cancel reason is required", groups = {AppointmentGroup.Cancel.class})
    private String cancelReason;

    public AppointmentDTO() {}

    public AppointmentDTO(Appointment appointment) {
        this.appointmentId = appointment.getAppointmentId();
        this.patientName = appointment.getPatientName();
        this.patientPhone = appointment.getPatientPhone();
        this.scheduleId = appointment.getSchedule().getScheduleId();
        this.patientId = appointment.getPatient().getUserId();
        this.date = appointment.getDate();
        this.cancelBy = appointment.getCancelBy() != null ? appointment.getCancelBy().getUserId() : null;
        this.cancelAt = appointment.getCancelAt();
        this.cancelReason = appointment.getCancelReason();
    }
}
