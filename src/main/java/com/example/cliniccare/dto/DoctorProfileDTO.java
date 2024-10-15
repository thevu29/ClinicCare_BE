package com.example.cliniccare.dto;

import com.example.cliniccare.model.DoctorProfile;
import com.example.cliniccare.model.User;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Data
@Getter
@Setter
public class DoctorProfileDTO {

    private UUID doctorProfileId;

    @NotBlank(message = "Specialty is required")
    private String specialty;

    private Date createAt;
    private Date deleteAt;

    private UserDTO user;

    public DoctorProfileDTO() {}

    public DoctorProfileDTO(DoctorProfile doctorProfile) {
        this.doctorProfileId = doctorProfile.getDoctorProfileId();
        this.specialty = doctorProfile.getSpecialty();
        this.createAt = doctorProfile.getCreateAt();
        this.deleteAt = doctorProfile.getDeleteAt();
        User user = doctorProfile.getUser();
        this.user = (user != null) ? new UserDTO(user) : null;    }

}
