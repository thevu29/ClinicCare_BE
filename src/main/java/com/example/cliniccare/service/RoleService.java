package com.example.cliniccare.service;

import com.example.cliniccare.dto.RoleDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.model.Role;
import com.example.cliniccare.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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

    public RoleDTO getRoleById(UUID id) throws NotFoundException {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role not found"));

        return new RoleDTO(role);
    }

    public RoleDTO createRole(RoleDTO roleDTO) throws BadRequestException {
        if (roleRepository.findByNameIgnoreCase(roleDTO.getName()) != null) {
            throw new BadRequestException("Name already exists");
        }

        Role role = new Role();
        role.setName(roleDTO.getName());
        role.setDescription(roleDTO.getDescription());

        Role savedRole = roleRepository.save(role);
        return new RoleDTO(savedRole);
    }

    public RoleDTO updateRole(UUID id, RoleDTO roleDTO) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role not found"));

        if (roleDTO.getName() != null && !roleDTO.getName().isEmpty()) {
            if (roleRepository.findByNameIgnoreCase(roleDTO.getName()) != null) {
                throw new BadRequestException("Name already exists");
            }

            role.setName(roleDTO.getName());
        }

        if (roleDTO.getDescription() != null && !roleDTO.getDescription().isEmpty()) {
            role.setDescription(roleDTO.getDescription());
        }

        Role savedRole = roleRepository.save(role);
        return new RoleDTO(savedRole);
    }
}
