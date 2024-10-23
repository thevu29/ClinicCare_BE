package com.example.cliniccare.repository;

import com.example.cliniccare.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {
    List<Schedule> findAllByOrderByDateTimeAsc();
    List<Schedule> findAllByDoctor_DoctorProfileIdOrderByDateTimeAsc(UUID doctorProfileId);
    List<Schedule> findAllByService_ServiceIdOrderByDateTimeAsc(UUID serviceId);
    List<Schedule> findByDateTimeBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);
}
