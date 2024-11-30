package com.example.cliniccare.repository;

import com.example.cliniccare.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, UUID>, JpaSpecificationExecutor<Feedback> {
    Optional<Feedback> findByFeedbackIdAndDeleteAtIsNull(UUID id);
    List<Feedback> findAllByFeedbackIdInAndDeleteAtIsNull(List<UUID> feedbackIds);
}
