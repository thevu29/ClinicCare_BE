package com.example.cliniccare.repository;

import com.example.cliniccare.dto.ServiceDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceDTO, UUID> {
}
