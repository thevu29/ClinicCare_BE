package com.example.cliniccare.repository;

import com.example.cliniccare.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Page<Role> findAllByNameContaining(String name, Pageable pageable);
    Optional<Role> findByNameIgnoreCase(String name);
}
