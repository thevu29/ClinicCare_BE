package com.example.cliniccare.repository;

import com.example.cliniccare.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findAllByOrderByDateDesc();
    List<Payment> findAllByPatient_UserIdOrderByDateDesc(UUID patientId);
}
