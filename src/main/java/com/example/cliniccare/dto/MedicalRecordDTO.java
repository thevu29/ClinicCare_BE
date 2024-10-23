package com.example.cliniccare.dto;

import com.example.cliniccare.interfaces.MedicalRecordGroup;
import com.example.cliniccare.model.MedicalRecord;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class MedicalRecordDTO {
    private UUID medicalRecordId;

    @NotNull(message = "Patient is required", groups = {MedicalRecordGroup.Create.class})
    private UUID patientId;

    @NotNull(message = "Doctor is required", groups = {MedicalRecordGroup.Create.class})
    private UUID doctorProfileId;

    @NotNull(message = "Service is required", groups = {MedicalRecordGroup.Create.class})
    private UUID serviceId;

    private String serviceName;

    private String description;

    private Date createAt;

    public MedicalRecordDTO() {
    }

    public MedicalRecordDTO(MedicalRecord medicalRecord) {
        this.medicalRecordId = medicalRecord.getMedicalRecordId();
        this.patientId = medicalRecord.getPatient().getUserId();
        this.doctorProfileId = medicalRecord.getDoctor().getDoctorProfileId();
        this.serviceId = medicalRecord.getService().getServiceId();
        this.serviceName = medicalRecord.getService().getName();
        this.description = medicalRecord.getDescription();
        this.createAt = medicalRecord.getCreateAt();
    }
}
