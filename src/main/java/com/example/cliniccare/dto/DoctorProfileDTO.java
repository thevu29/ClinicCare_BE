package com.example.cliniccare.dto;

import com.example.cliniccare.interfaces.DoctorProfileGroup;
import com.example.cliniccare.interfaces.PromotionFormGroup;
import com.example.cliniccare.interfaces.UserFormGroup;
import com.example.cliniccare.model.DoctorProfile;
import com.example.cliniccare.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "Specialty is required", groups = {DoctorProfileGroup.Create.class})
    private String specialty;

    private Date createAt;

    private Date deleteAt;

    @NotNull(message = "User is required", groups = {DoctorProfileGroup.Create.class})
    private UUID userId;

    public DoctorProfileDTO() {
    }

    public DoctorProfileDTO(DoctorProfile doctorProfile) {
        this.doctorProfileId = doctorProfile.getDoctorProfileId();
        this.specialty = doctorProfile.getSpecialty();
        this.createAt = doctorProfile.getCreateAt();
        this.deleteAt = doctorProfile.getDeleteAt();
        this.userId = doctorProfile.getUser().getUserId();
    }
}
