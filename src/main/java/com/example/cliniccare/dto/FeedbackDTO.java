package com.example.cliniccare.dto;

import com.example.cliniccare.interfaces.FeedbackFormGroup;
import com.example.cliniccare.entity.Feedback;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class FeedbackDTO {
    private UUID feedbackId;

    @NotNull(message = "Patient is required", groups = {FeedbackFormGroup.Create.class})
    private UUID patientId;

    private String patientName;

    private String image;

    private UUID doctorId;

    private String doctorName;

    private UUID serviceId;

    private String serviceName;

    private LocalDateTime date;

    @NotBlank(message = "Feedback content is required", groups = {FeedbackFormGroup.Create.class, FeedbackFormGroup.Update.class})
    private String feedback;

    public FeedbackDTO(Feedback feedback) {
        this.feedbackId = feedback.getFeedbackId();
        this.patientId = feedback.getPatient().getUserId();
        this.patientName = feedback.getPatient().getName();
        this.image = feedback.getPatient().getImage();
        this.doctorId = feedback.getDoctor() == null ? null : feedback.getDoctor().getDoctorProfileId();
        this.doctorName = feedback.getDoctor() == null ? null : feedback.getDoctor().getUser().getName();
        this.serviceId = feedback.getService() == null ? null : feedback.getService().getServiceId();
        this.serviceName = feedback.getService() == null ? null : feedback.getService().getName();
        this.date = feedback.getCreateAt();
        this.feedback = feedback.getFeedback();
    }
}
