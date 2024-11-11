package com.example.cliniccare.service;

import com.example.cliniccare.dto.PaginationDTO;
import com.example.cliniccare.dto.RoleDTO;
import com.example.cliniccare.dto.UserDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.model.Role;
import com.example.cliniccare.model.User;
import com.example.cliniccare.repository.RoleRepository;
import com.example.cliniccare.response.PaginationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final PaginationService paginationService;

    @Autowired
    public RoleService(RoleRepository roleRepository, PaginationService paginationService) {
        this.roleRepository = roleRepository;
        this.paginationService = paginationService;
    }

    public PaginationResponse<List<RoleDTO>> getRoles(PaginationDTO paginationQuery, String search) {
        Pageable pageable = paginationService.getPageable(paginationQuery);

        Page<Role> roles = search.isEmpty()
                ? roleRepository.findAll(pageable)
                : roleRepository.findAllByNameContaining(search, pageable);

        int totalPages = roles.getTotalPages();
        long totalElements = roles.getTotalElements();
        int take = roles.getNumberOfElements();

        return new PaginationResponse<>(
                true,
                "Get users successfully",
                roles.map(RoleDTO::new).getContent(),
                paginationQuery.page,
                paginationQuery.size,
                take,
                totalPages,
                totalElements
        );
    }

    public RoleDTO getRoleById(UUID id) throws NotFoundException {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role not found"));

        return new RoleDTO(role);
    }

    public RoleDTO createRole(RoleDTO roleDTO) throws BadRequestException {
        if (roleRepository.findByNameIgnoreCase(roleDTO.getName()).orElse(null) != null) {
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
            if (roleRepository.findByNameIgnoreCase(roleDTO.getName()).orElse(null) != null) {
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
