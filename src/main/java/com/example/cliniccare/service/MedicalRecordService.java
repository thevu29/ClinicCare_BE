package com.example.cliniccare.service;

import com.example.cliniccare.dto.MedicalRecordDTO;
import com.example.cliniccare.dto.PaginationDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.model.DoctorProfile;
import com.example.cliniccare.model.MedicalRecord;
import com.example.cliniccare.model.User;
import com.example.cliniccare.repository.DoctorProfileRepository;
import com.example.cliniccare.repository.MedicalRecordRepository;
import com.example.cliniccare.repository.ServiceRepository;
import com.example.cliniccare.repository.UserRepository;
import com.example.cliniccare.response.PaginationResponse;
import com.example.cliniccare.utils.DateQueryParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MedicalRecordService {
    private final MedicalRecordRepository medicalRecordRepository;
    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final ServiceRepository serviceRepository;
    private final PaginationService paginationService;

    @Autowired
    public MedicalRecordService(
            MedicalRecordRepository medicalRecordRepository,
            UserRepository userRepository,
            DoctorProfileRepository doctorProfileRepository,
            ServiceRepository serviceRepository,
            PaginationService paginationService
    ) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.userRepository = userRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.serviceRepository = serviceRepository;
        this.paginationService = paginationService;
    }

    public PaginationResponse<List<MedicalRecordDTO>> getMedicalRecord(
            PaginationDTO paginationDTO, String search, String date, UUID patientId, UUID doctorId, UUID serviceId
    ) {
        Pageable pageable = paginationService.getMedicalRecordPageable(paginationDTO);

        Specification<MedicalRecord> spec = Specification.where((root, query, cb) ->
                cb.isNull(root.get("deleteAt")));

        if (search != null && !search.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(root.get("patient").get("name"), "%" + search + "%"),
                    cb.like(root.get("service").get("name"), "%" + search + "%"),
                    cb.like(root.get("doctor").get("user").get("name"), "%" + search + "%")
            ));
        }
        if (date != null && !date.isEmpty()) {
            DateQueryParser<MedicalRecord> dateQueryParser = new DateQueryParser<>(date, "createAt");
            spec = spec.and(dateQueryParser.createDateSpecification());
        }
        if (patientId != null) {
            User patient = userRepository.findByUserIdAndDeleteAtIsNull(patientId)
                    .orElseThrow(() -> new NotFoundException("Patient not found"));

            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("patient").get("userId"), patient.getUserId()));
        }
        if (doctorId != null) {
            DoctorProfile doctor = doctorProfileRepository.findByDoctorProfileIdAndDeleteAtIsNull(doctorId)
                    .orElseThrow(() -> new NotFoundException("Doctor not found"));

            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("doctor").get("doctorProfileId"), doctor.getDoctorProfileId()));
        }
        if (serviceId != null) {
            com.example.cliniccare.model.Service service = serviceRepository.findById(serviceId)
                    .orElseThrow(() -> new NotFoundException("Service not found"));

            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("service").get("serviceId"), service.getServiceId()));
        }

        Page<MedicalRecord> medicalRecords = medicalRecordRepository.findAll(spec, pageable);

        int totalPages = medicalRecords.getTotalPages();
        long totalElements = medicalRecords.getTotalElements();
        int take = medicalRecords.getNumberOfElements();

        return new PaginationResponse<>(
                true,
                "Get medical records successfully",
                medicalRecords.map(MedicalRecordDTO::new).toList(),
                paginationDTO.page,
                paginationDTO.size,
                take,
                totalPages,
                totalElements
        );
    }

    public MedicalRecordDTO getMedicalRecordById(UUID id) {
        MedicalRecord medicalRecord = medicalRecordRepository.findByMedicalRecordIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Medical Record not found"));

        return new MedicalRecordDTO(medicalRecord);
    }

    public MedicalRecordDTO createMedicalRecord(MedicalRecordDTO medicalRecordDTO) {
        User user = userRepository.findByUserIdAndDeleteAtIsNull(medicalRecordDTO.getPatientId())
                .orElseThrow(() -> new NotFoundException("Patient not found"));

        if (!user.getRole().getName().equalsIgnoreCase("user")) {
            throw new BadRequestException("User id is not a patient");
        }

        DoctorProfile doctorProfile = doctorProfileRepository
                .findByDoctorProfileIdAndDeleteAtIsNull(medicalRecordDTO.getDoctorProfileId())
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        if (!doctorProfile.getUser().getRole().getName().equalsIgnoreCase("doctor")) {
            throw new BadRequestException("Doctor id is not a doctor");
        }

        com.example.cliniccare.model.Service service = serviceRepository
                .findById(medicalRecordDTO.getServiceId())
                .orElseThrow(() -> new NotFoundException("Service not found"));

        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setDescription(medicalRecordDTO.getDescription());
        medicalRecord.setPatient(user);
        medicalRecord.setDoctor(doctorProfile);
        medicalRecord.setService(service);

        MedicalRecord savedMedicalRecord = medicalRecordRepository.save(medicalRecord);
        return new MedicalRecordDTO(savedMedicalRecord);
    }

    public MedicalRecordDTO updateMedicalRecord(UUID id, MedicalRecordDTO medicalRecordDTO) {
        System.out.println(medicalRecordDTO.getDescription());
        MedicalRecord medicalRecord = medicalRecordRepository
                .findByMedicalRecordIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Medical Record not found"));

        if (medicalRecordDTO.getDescription() != null) {
            medicalRecord.setDescription(medicalRecordDTO.getDescription());
        }

        if(medicalRecordDTO.getPatientId() != null){
            medicalRecord.setPatient(userRepository.findById(medicalRecordDTO.getPatientId())
                    .orElseThrow(() -> new NotFoundException("Patient not found")));
        }

        if (medicalRecordDTO.getDoctorProfileId() != null) {
            medicalRecord.setDoctor(doctorProfileRepository.findById(medicalRecordDTO.getDoctorProfileId())
                    .orElseThrow(() -> new NotFoundException("Doctor not found")));
        }

        if (medicalRecordDTO.getServiceId() != null) {
            medicalRecord.setService(serviceRepository.findById(medicalRecordDTO.getServiceId())
                    .orElseThrow(() -> new NotFoundException("Service not found")));
        }

        MedicalRecord savedMedicalRecord = medicalRecordRepository.save(medicalRecord);
        return new MedicalRecordDTO(savedMedicalRecord);
    }

    public void deleteMedicalRecord(UUID id) {
        MedicalRecord medicalRecord = medicalRecordRepository.findByMedicalRecordIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Medical Record not found"));

        medicalRecord.setDeleteAt(LocalDateTime.now());
        medicalRecordRepository.save(medicalRecord);
    }
}
