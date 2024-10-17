package com.example.cliniccare.repository;

import com.example.cliniccare.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {
    Optional<Feedback> findByFeedbackIdAndDeleteAtIsNull(UUID id);
}
