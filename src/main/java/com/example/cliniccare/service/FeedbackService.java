package com.example.cliniccare.service;

import com.example.cliniccare.dto.FeedbackDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.model.DoctorProfile;
import com.example.cliniccare.model.Feedback;
import com.example.cliniccare.model.Service;
import com.example.cliniccare.model.User;
import com.example.cliniccare.repository.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@org.springframework.stereotype.Service
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final ServiceRepository serviceRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;

    @Autowired
    public FeedbackService(FeedbackRepository feedbackRepository,
                           ServiceRepository serviceRepository,
                           DoctorProfileRepository doctorProfileRepository, UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.serviceRepository = serviceRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.userRepository = userRepository;
    }

    public List<FeedbackDTO> getFeedbacks() {
        return feedbackRepository.findAllByDeleteAtIsNull().stream().map(FeedbackDTO::new).toList();
    }

    public List<FeedbackDTO> getPatientFeedbacks(UUID patientId) {
        return feedbackRepository
                .findAllByPatientUserIdAndDeleteAtIsNull(patientId)
                .stream()
                .map(FeedbackDTO::new).toList();
    }

    public FeedbackDTO getFeedbackById(UUID id) {
        Feedback feedback = feedbackRepository.findByFeedbackIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Feedback not found"));

        return new FeedbackDTO(feedback);
    }

    public FeedbackDTO createFeedback(FeedbackDTO feedbackDTO) {
        User patient = userRepository
                .findByUserIdAndDeleteAtIsNull(feedbackDTO.getPatientId())
                .orElseThrow(() -> new NotFoundException("Patient not found"));

        if (feedbackDTO.getDoctorId() == null && feedbackDTO.getServiceId() == null) {
            throw new BadRequestException("Doctor or Service is required");
        }
        if (feedbackDTO.getDoctorId() != null && feedbackDTO.getServiceId() != null) {
            throw new BadRequestException("Doctor and Service cannot be set at the same time");
        }

        Feedback feedback = new Feedback();

        if (feedbackDTO.getDoctorId() != null) {
            DoctorProfile doctor = doctorProfileRepository
                    .findById(feedbackDTO.getDoctorId())
                    .orElseThrow(() -> new NotFoundException("Doctor not found"));
            feedback.setDoctor(doctor);
        }

        if (feedbackDTO.getServiceId() != null) {
            Service service = serviceRepository
                    .findById(feedbackDTO.getServiceId())
                    .orElseThrow(() -> new NotFoundException("Service not found"));
            feedback.setService(service);
        }

        feedback.setPatient(patient);
        feedback.setFeedback(feedbackDTO.getFeedback());

        return new FeedbackDTO(feedbackRepository.save(feedback));
    }

    public FeedbackDTO updateFeedback(UUID id, FeedbackDTO feedbackDTO) {
        Feedback feedback = feedbackRepository
                .findByFeedbackIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Feedback not found"));

        feedback.setFeedback(feedbackDTO.getFeedback());

        return new FeedbackDTO(feedbackRepository.save(feedback));
    }

    public void deleteFeedback(UUID id) {
        Feedback feedback = feedbackRepository
                .findByFeedbackIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Feedback not found"));

        feedback.setDeleteAt(LocalDateTime.now());
        feedbackRepository.save(feedback);
    }
}
