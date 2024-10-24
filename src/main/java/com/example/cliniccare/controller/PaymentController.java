package com.example.cliniccare.controller;

import com.example.cliniccare.dto.PaymentDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.response.ApiResponse;
import com.example.cliniccare.service.PaymentService;
import com.example.cliniccare.validation.Validation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public ResponseEntity<?> getPayments() {
        try {
            List<PaymentDTO> payments = paymentService.getPayments();

            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Payments retrieved successfully", payments
            ));
        } catch (Exception e) {
            logger.error("Failed to retrieve payments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, "Failed to retrieve payments", null
            ));
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getPatientPayments(@PathVariable UUID patientId) {
        try {
            List<PaymentDTO> payments = paymentService.getPatientPayments(patientId);

            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Patient payments retrieved successfully", payments
            ));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (Exception e) {
            logger.error("Failed to retrieve patient payments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, "Failed to retrieve patient payments", null
            ));
        }
    }

    @PostMapping
    public ResponseEntity<?> createPayment(
            @Valid @RequestBody PaymentDTO paymentDTO,
            BindingResult bindingResult
    ) {
        try {
            if (Validation.validateBody(bindingResult) != null) {
                return Validation.validateBody(bindingResult);
            }

            PaymentDTO payment = paymentService.createPayment(paymentDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                    true, "Payment created successfully", payment
            ));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (Exception e) {
            logger.error("Failed to create payment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, "Failed to create payment", null
            ));
        }
    }

    @PutMapping("/change-status/{paymentId}")
    public ResponseEntity<?> changePaymentStatus(
            @PathVariable UUID paymentId,
            @RequestBody Map<String, String> statusMap
    ) {
        try {
            String status = statusMap.get("status");
            PaymentDTO payment = paymentService.changePaymentStatus(paymentId, status);

            return ResponseEntity.ok(new ApiResponse<>(
                    true, "Payment status changed successfully", payment
            ));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(
                    false, e.getMessage(), null
            ));
        } catch (Exception e) {
            logger.error("Failed to change payment status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, "Failed to change payment status", null
            ));
        }
    }
}
