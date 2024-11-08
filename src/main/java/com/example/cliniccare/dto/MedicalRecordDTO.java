package com.example.cliniccare.dto;

import com.example.cliniccare.interfaces.MedicalRecordGroup;
import com.example.cliniccare.model.MedicalRecord;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MedicalRecordDTO {
    private UUID medicalRecordId;

    @NotNull(message = "Patient is required", groups = {MedicalRecordGroup.Create.class})
    private UUID patientId;

    private String patientName;

    @NotNull(message = "Doctor is required", groups = {MedicalRecordGroup.Create.class})
    private UUID doctorProfileId;

    private String doctorName;

    @NotNull(message = "Service is required", groups = {MedicalRecordGroup.Create.class})
    private UUID serviceId;

    private String doctorName;

    private String patientName;

    private String serviceName;

    private String description;

    private LocalDateTime date;

    public MedicalRecordDTO() {
    }

    public MedicalRecordDTO(MedicalRecord medicalRecord) {
        this.medicalRecordId = medicalRecord.getMedicalRecordId();
        this.patientId = medicalRecord.getPatient().getUserId();
        this.patientName = medicalRecord.getPatient().getName();
        this.doctorProfileId = medicalRecord.getDoctor().getDoctorProfileId();
        this.doctorName = medicalRecord.getDoctor().getUser().getName();
        this.serviceId = medicalRecord.getService().getServiceId();
        this.serviceName = medicalRecord.getService().getName();
        this.description = medicalRecord.getDescription();
        this.date = medicalRecord.getCreateAt();
    }
}
