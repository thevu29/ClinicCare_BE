package com.example.cliniccare.dto;

import com.example.cliniccare.model.MedicalRecord;
import jakarta.validation.constraints.NotBlank;
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
    private UserDTO patient;
    private DoctorProfileDTO doctor;
    private String service;
    private String message;
    private Date createAt;
    private Date deleteAt;

    public MedicalRecordDTO() {
    }

    public MedicalRecordDTO(MedicalRecord medicalRecord) {
        this.medicalRecordId = medicalRecord.getMedicalRecordId();
        this.patient = new UserDTO(medicalRecord.getPatient());
        this.doctor = new DoctorProfileDTO(medicalRecord.getDoctor());
        this.service = null;
        this.message = medicalRecord.getMessage();
        this.createAt = medicalRecord.getCreateAt();
        this.deleteAt = medicalRecord.getDeleteAt();
    }
}
