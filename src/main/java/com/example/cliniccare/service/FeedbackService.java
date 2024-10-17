package com.example.cliniccare.service;

import com.example.cliniccare.dto.FeedbackDTO;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.model.DoctorProfile;
import com.example.cliniccare.model.Feedback;
import com.example.cliniccare.model.Service;
import com.example.cliniccare.model.User;
import com.example.cliniccare.repository.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
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
        try {
            return feedbackRepository.findAll().stream().map(FeedbackDTO::new).toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get feedbacks", e);
        }
    }

    public FeedbackDTO getFeedbackById(UUID id) {
        Feedback feedback = feedbackRepository.findByFeedbackIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Feedback not found"));

        return new FeedbackDTO(feedback);
    }

    public FeedbackDTO createFeedback(FeedbackDTO feedbackDTO) {
//        Find if exist patientId
        User patient = userRepository
                .findByUserIdAndDeleteAtIsNull(feedbackDTO.getPatientId())
                .orElseThrow(() -> new NotFoundException("Patient not found"));

//        Find if exist doctorId
        DoctorProfile doctor = doctorProfileRepository
                .findById(feedbackDTO.getDoctorId())
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

//        Find if exist serviceId
        Service service = serviceRepository
                .findById(feedbackDTO.getServiceId())
                .orElseThrow(() -> new NotFoundException("Service not found"));

        Feedback feedback = new Feedback();
        feedback.setPatient(patient);
        feedback.setDoctor(doctor);
        feedback.setService(service);
        feedback.setDate(feedbackDTO.getDate());
        feedback.setFeedback(feedbackDTO.getFeedback());

        return new FeedbackDTO(feedbackRepository.save(feedback));
    }

    public FeedbackDTO updateFeedback(UUID id, FeedbackDTO feedbackDTO) {
        Feedback feedback = feedbackRepository
                .findByFeedbackIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Feedback not found"));

        if (feedbackDTO.getPatientId() != null) {
            //        Find if exist userId
            User patient = userRepository
                    .findByUserIdAndDeleteAtIsNull(feedbackDTO.getPatientId())
                    .orElseThrow(() -> new NotFoundException("Patient not found"));

            feedback.setPatient(patient);
        }

        if (feedbackDTO.getDoctorId() != null) {
            //        Find if exist doctorId
            DoctorProfile doctor = doctorProfileRepository
                    .findById(feedbackDTO.getDoctorId())
                    .orElseThrow(() -> new NotFoundException("Doctor not found"));

            feedback.setDoctor(doctor);
        }

        if (feedbackDTO.getServiceId() != null) {
            //        Find if exist serviceId
            Service service = serviceRepository
                    .findById(feedbackDTO.getServiceId())
                    .orElseThrow(() -> new NotFoundException("Service not found"));

            feedback.setService(service);
        }

        if (feedbackDTO.getDate() != null) {
            feedback.setDate(feedbackDTO.getDate());
        }

        if (feedbackDTO.getFeedback() != null) {
            feedback.setFeedback(feedbackDTO.getFeedback());
        }

        return new FeedbackDTO(feedbackRepository.save(feedback));
    }

    public void deleteFeedback(UUID id) {
        Feedback feedback = feedbackRepository
                .findByFeedbackIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Feedback not found"));

        feedback.setDeleteAt(new Date());
        feedbackRepository.save(feedback);
    }
}
