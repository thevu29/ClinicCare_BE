package com.example.cliniccare.service;

import com.example.cliniccare.dto.FeedbackDTO;
import com.example.cliniccare.dto.PaginationDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.entity.*;
import com.example.cliniccare.repository.*;
import com.example.cliniccare.response.PaginationResponse;
import com.example.cliniccare.utils.DateQueryParser;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final ServiceRepository serviceRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;
    private final PaginationService paginationService;

    @Autowired
    public FeedbackService(
            FeedbackRepository feedbackRepository,
            ServiceRepository serviceRepository,
            DoctorProfileRepository doctorProfileRepository,
            UserRepository userRepository,
            PaginationService paginationService
    ) {
        this.feedbackRepository = feedbackRepository;
        this.serviceRepository = serviceRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.userRepository = userRepository;
        this.paginationService = paginationService;
    }

    @Transactional(readOnly = true)
    public List<FeedbackDTO> getAllFeedbacks() {
        return feedbackRepository.findAll().stream()
                .map(FeedbackDTO::new)
                .collect(Collectors.toList());
    }

    public PaginationResponse<List<FeedbackDTO>> getFeedbacks(
            PaginationDTO paginationDTO,
            String search,
            String date,
            UUID userId,
            UUID patientId,
            UUID serviceId
    ) {
        Pageable pageable = paginationService.getFeedbackPageable(paginationDTO);

        Specification<Feedback> spec = Specification.where((root, query, cb) -> {
            if (query != null && query.getResultType().equals(Feedback.class)) {
                root.fetch("patient", JoinType.LEFT);
                root.fetch("doctor", JoinType.LEFT).fetch("user", JoinType.LEFT);
                root.fetch("service", JoinType.LEFT);
            }
            return cb.isNull(root.get("deleteAt"));
        });

        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();

            spec = spec.and((root, query, cb) -> {
                if (query != null)  query.distinct(true);

                Join<Feedback, User> patientJoin = root.join("patient", JoinType.LEFT);
                Join<Feedback, DoctorProfile> doctorJoin = root.join("doctor", JoinType.LEFT);
                Join<DoctorProfile, User> userJoin = doctorJoin.join("user", JoinType.LEFT);
                Join<Feedback, Service> serviceJoin = root.join("service", JoinType.LEFT);

                return cb.or(
                        cb.like(cb.lower(root.get("feedback")), "%" + searchLower + "%"),
                        cb.like(cb.lower(patientJoin.get("name")), "%" + searchLower + "%"),
                        cb.like(cb.lower(userJoin.get("name")), "%" + searchLower + "%"),
                        cb.like(cb.lower(serviceJoin.get("name")), "%" + searchLower + "%")
                );
            });
        }

        if (patientId != null) {
            User patient = userRepository
                    .findByUserIdAndDeleteAtIsNull(patientId)
                    .orElseThrow(() -> new NotFoundException("Patient not found"));

            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("patient").get("userId"), patient.getUserId()));
        }

        if (userId != null) {
            DoctorProfile doctor = doctorProfileRepository
                    .findByUser_UserIdAndDeleteAtIsNull(userId)
                    .orElseThrow(() -> new NotFoundException("Doctor not found"));

            spec = spec.and((root, query, cb)
                    -> cb.equal(root.get("doctor").get("doctorProfileId"), doctor.getDoctorProfileId()));
        }

        if (serviceId != null) {
            Service service = serviceRepository
                    .findById(serviceId)
                    .orElseThrow(() -> new NotFoundException("Service not found"));

            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("service").get("serviceId"), service.getServiceId()));
        }

        if (date != null && !date.trim().isEmpty()) {
            DateQueryParser<Feedback> dateParser = new DateQueryParser<>(date, "createAt");
            Specification<Feedback> dateSpec = dateParser.createDateSpecification();
            spec = spec.and(dateSpec);
        }

        Page<Feedback> feedbacks = feedbackRepository.findAll(spec, pageable);

        int totalPages = feedbacks.getTotalPages();
        long totalElements = feedbacks.getTotalElements();
        int take = feedbacks.getNumberOfElements();

        return new PaginationResponse<>(
                true,
                "Get feedbacks successfully",
                feedbacks.map(FeedbackDTO::new).getContent(),
                paginationDTO.page,
                paginationDTO.size,
                take,
                totalPages,
                totalElements
        );
    }

    @Transactional(readOnly = true)
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

    public void deleteFeedbacks(List<UUID> ids) {
        List<Feedback> feedbacks = feedbackRepository
                .findAllByFeedbackIdInAndDeleteAtIsNull(ids);

        if (feedbacks.isEmpty()) {
            throw new NotFoundException("No feedbacks found for the provided IDs");
        }

        feedbacks.forEach(feedback -> feedback.setDeleteAt(LocalDateTime.now()));

        feedbackRepository.saveAll(feedbacks);
    }
}
