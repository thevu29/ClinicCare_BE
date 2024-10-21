package com.example.cliniccare.dto;

import com.example.cliniccare.interfaces.FeedbackFormGroup;
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
    private UUID feedbackId;

    @NotNull(message = "Patient is required", groups = {FeedbackFormGroup.Create.class})
    private UUID patientId;

    private UUID doctorId;

    private UUID serviceId;

    private Date date;

    @NotBlank(message = "Feedback content is required", groups = {FeedbackFormGroup.Create.class, FeedbackFormGroup.Update.class})
    private String feedback;

    public FeedbackDTO(Feedback feedback) {
        this.feedbackId = feedback.getFeedbackId();
        this.patientId = feedback.getPatient().getUserId();
        this.doctorId = feedback.getDoctor() == null ? null : feedback.getDoctor().getDoctorProfileId();
        this.serviceId = feedback.getService() == null ? null : feedback.getService().getServiceId();
        this.date = feedback.getCreateAt();
        this.feedback = feedback.getFeedback();
    }
}
