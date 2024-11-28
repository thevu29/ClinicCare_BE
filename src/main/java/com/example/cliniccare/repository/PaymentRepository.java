package com.example.cliniccare.repository;

import com.example.cliniccare.model.Payment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment> {
    List<Payment> findAllByPatient_UserIdOrderByDateDesc(UUID patientId);

    @Query("SELECT SUM(p.totalPrice) FROM Payment p WHERE MONTH(p.date) = :month AND YEAR(p.date) = :year")
    Double calculateMonthlyProfit(@Param("month") int month, @Param("year") int year);

    @Query("SELECT p.service.serviceId, COUNT(p) AS usageCount " +
            "FROM Payment p " +
            "WHERE p.status = 1 " +
            "GROUP BY p.service.serviceId " +
            "ORDER BY usageCount DESC")
    List<Object[]> findTopServices(Pageable pageable);
}
