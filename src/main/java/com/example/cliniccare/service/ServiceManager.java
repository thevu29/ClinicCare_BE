package com.example.cliniccare.service;

import com.example.cliniccare.dto.ServiceDTO;
import com.example.cliniccare.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServiceManager {
    private final ServiceRepository serviceRepository;

    @Autowired
    public ServiceManager(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public ServiceDTO createService(ServiceDTO serviceDTO) {
        return serviceRepository.save(serviceDTO);
    }
}
