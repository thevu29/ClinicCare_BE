package com.example.cliniccare.service;

import com.example.cliniccare.dto.RoleDTO;
import com.example.cliniccare.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<RoleDTO> getRoles() {
        try {
            return roleRepository.findAll().stream().map(RoleDTO::new).toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get roles", e);
        }
    }
}
