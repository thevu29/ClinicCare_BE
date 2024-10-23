package com.example.cliniccare.controller;

import com.example.cliniccare.dto.FeedbackDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.interfaces.FeedbackFormGroup;
import com.example.cliniccare.response.ApiResponse;
import com.example.cliniccare.service.FeedbackService;
import com.example.cliniccare.validation.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {
    private static final Logger logger = LoggerFactory.getLogger(FeedbackController.class);
    private final FeedbackService feedbackService;

    @Autowired
    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping
    public ResponseEntity<?> getFeedbacks() {
        try {
            List<FeedbackDTO> feedbacks = feedbackService.getFeedbacks();
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Get feedbacks successfully", feedbacks
            ));
        } catch (Exception e) {
            logger.error("Failed to get feedbacks: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getPatientFeedbacks(@PathVariable UUID patientId) {
        try {
            List<FeedbackDTO> feedbacks = feedbackService.getPatientFeedbacks(patientId);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Get patient feedbacks successfully", feedbacks
            ));
        } catch (Exception e) {
            logger.error("Failed to get patient feedbacks: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFeedbackById(@PathVariable UUID id) {
        try {
            FeedbackDTO feedback = feedbackService.getFeedbackById(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Get feedback successfully", feedback
            ));
        } catch (NotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Failed to get feedback: {}", e.getMessage(), e);
            return ResponseEntity
                    .internalServerError()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping
    public ResponseEntity<?> createFeedback(
            @Validated(FeedbackFormGroup.Create.class) @RequestBody FeedbackDTO feedbackDTO,
            BindingResult bindingResult
    ) {
        try {
            if (Validation.validateBody(bindingResult) != null) {
                return Validation.validateBody(bindingResult);
            }

            FeedbackDTO feedback = feedbackService.createFeedback(feedbackDTO);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(
                            true, "Create feedback successfully", feedback
                    ));
        } catch (NotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(
                            false, e.getMessage(), null
                    ));
        } catch (BadRequestException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(
                            false, e.getMessage(), null
                    ));
        } catch (Exception e) {
            logger.error("Failed to create feedback: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                            false, "Failed to create feedback", null
                    ));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateFeedback(
            @PathVariable UUID id,
            @Validated(FeedbackFormGroup.Update.class) @RequestBody FeedbackDTO feedbackDTO,
            BindingResult bindingResult
    ) {
        if (Validation.validateBody(bindingResult) != null) {
            return Validation.validateBody(bindingResult);
        }

        try {
            FeedbackDTO feedback = feedbackService.updateFeedback(id, feedbackDTO);

            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Update feedback successfully", feedback
            ));
        } catch (NotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(
                            false, e.getMessage(), null
                    ));
        } catch (Exception e) {
            logger.error("Failed to update feedback: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                            false, "Failed to update feedback", null
                    ));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteFeedback(@PathVariable UUID id) {
        try {
            feedbackService.deleteFeedback(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Delete feedback successfully", null
            ));
        } catch (NotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Failed to delete feedback: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                            false, "Failed to delete feedback", null
                    ));
        }
    }
}
