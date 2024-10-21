package com.example.cliniccare.repository;

import com.example.cliniccare.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<Service, UUID> {
    Optional<Service> findByServiceId(UUID id);
    List<Service> findAllByDeleteAtIsNull();
    Optional<Service> findByServiceIdAndDeleteAtIsNull(UUID id);
}
