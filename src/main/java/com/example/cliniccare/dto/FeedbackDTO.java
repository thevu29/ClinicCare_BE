package com.example.cliniccare.dto;

import com.example.cliniccare.model.Feedback;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class FeedbackDTO {
    @NotNull(message = "FeedbackId is required")
    private UUID feedbackId;

    @NotNull(message = "PatientId is required")
    private UUID patientId;

    @NotNull(message = "DoctorId is required")
    private UUID doctorId;

    @NotNull(message = "ServiceId is required")
    private UUID serviceId;

    @NotNull(message = "Date is required")
    private Date date;

    @NotBlank(message = "Feedback content is required")
    private String feedback;

    private Date createAt;

    private Date deleteAt;

    public FeedbackDTO(Feedback feedback) {
        this.feedbackId = feedback.getFeedbackId();
        this.patientId = feedback.getPatient().getUserId();
        this.doctorId = feedback.getDoctor().getDoctorProfileId();
        this.serviceId = feedback.getService().getServiceId();
        this.date = feedback.getDate();
        this.feedback = feedback.getFeedback();
        this.createAt = feedback.getCreateAt();
        this.deleteAt = feedback.getDeleteAt();
    }
}
