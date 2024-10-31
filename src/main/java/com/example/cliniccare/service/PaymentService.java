package com.example.cliniccare.service;

import com.example.cliniccare.dto.PaymentDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.model.Payment;
import com.example.cliniccare.model.Service;
import com.example.cliniccare.model.User;
import com.example.cliniccare.repository.PaymentRepository;
import com.example.cliniccare.repository.ServiceRepository;
import com.example.cliniccare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@org.springframework.stereotype.Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserRepository patientRepository;
    private final ServiceRepository serviceRepository;

    @Autowired
    public PaymentService(
            PaymentRepository paymentRepository,
            UserRepository patientRepository,
            ServiceRepository serviceRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.patientRepository = patientRepository;
        this.serviceRepository = serviceRepository;
    }

    private Payment.PaymentStatus getPaymentStatus(String status) {
        try {
            System.out.println(status);
            return status != null && !status.isEmpty()
                    ? Payment.PaymentStatus.valueOf(status.toUpperCase())
                    : Payment.PaymentStatus.PENDING;
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Invalid payment status");
        }
    }

    private Payment.PaymentMethod getPaymentMethod(String method) {
        try {
            return Payment.PaymentMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Invalid payment method: CASH or BANKING");
        }
    }

    public List<PaymentDTO> getPayments() {
        List<Payment> payments = paymentRepository.findAllByOrderByDateDesc();
        return payments.stream().map(PaymentDTO::new).toList();
    }

    public List<PaymentDTO> getPatientPayments(UUID patientId) {
        User patient = patientRepository.findByUserIdAndDeleteAtIsNull(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found"));

        List<Payment> payments = paymentRepository.findAllByPatient_UserIdOrderByDateDesc(patient.getUserId());
        return payments.stream().map(PaymentDTO::new).toList();
    }

    @Transactional
    public PaymentDTO createPayment(PaymentDTO paymentDTO) {
        User patient = patientRepository.findById(paymentDTO.getPatientId())
                .orElseThrow(() -> new NotFoundException("Patient not found"));

        Service service = serviceRepository.findById(paymentDTO.getServiceId())
                .orElseThrow(() -> new NotFoundException("Service not found"));

        Payment payment = new Payment();
        payment.setPatient(patient);
        payment.setService(service);
        payment.setMethod(getPaymentMethod(paymentDTO.getMethod()));

        Payment savedPayment = paymentRepository.save(payment);

        return new PaymentDTO(savedPayment);
    }

    @Transactional
    public PaymentDTO changePaymentStatus(UUID paymentId, String status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        if (payment.getStatus() == Payment.PaymentStatus.PAID) {
            throw new BadRequestException("Payment already paid");
        }

        payment.setStatus(getPaymentStatus(status));

        Payment updatedPayment = paymentRepository.save(payment);

        return new PaymentDTO(updatedPayment);
    }
}
