package com.example.cliniccare.dto;

import com.example.cliniccare.model.DoctorProfile;
import com.example.cliniccare.model.MedicalRecord;
import com.example.cliniccare.model.Service;
import com.example.cliniccare.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Setter
@Getter
@Data
public class MedicalRecordDTO {
    private UUID medicalRecordId;

    @NotNull(message = "Patient is required")
    private UUID patientId;

    @NotNull(message = "Doctor is required")
    private UUID doctorProfileId;

    @NotNull(message = "Service is required")
    private UUID serviceId;

    @NotBlank(message = "Message cannot be blank")
    private String message;

    private Date createAt;

    private Date deleteAt;

    public MedicalRecordDTO() {
    }

    public MedicalRecordDTO(MedicalRecord medicalRecord) {
        this.medicalRecordId = medicalRecord.getMedicalRecordId();
        this.patientId = medicalRecord.getPatient().getUserId();
        this.doctorProfileId = medicalRecord.getDoctor().getDoctorProfileId();
        this.serviceId = medicalRecord.getService().getServiceId();
        this.message = medicalRecord.getMessage();
        this.createAt = medicalRecord.getCreateAt();
        this.deleteAt = medicalRecord.getDeleteAt();
    }
}
