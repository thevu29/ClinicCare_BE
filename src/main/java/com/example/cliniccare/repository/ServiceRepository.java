package com.example.cliniccare.repository;

import com.example.cliniccare.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<Service, UUID>, JpaSpecificationExecutor<Service> {
    List<Service> findAllByDeleteAtIsNull();
    Optional<Service> findByServiceId(UUID id);
    Optional<Service> findByServiceIdAndDeleteAtIsNull(UUID id);
}
