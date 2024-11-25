package com.example.cliniccare.repository;

import com.example.cliniccare.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, UUID>, JpaSpecificationExecutor<Schedule> {
    List<Schedule> findAllByOrderByDateTimeDesc();
    List<Schedule> findByDateTimeBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);
}
