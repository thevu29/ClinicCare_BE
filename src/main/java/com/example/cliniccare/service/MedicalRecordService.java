package com.example.cliniccare.service;

import com.example.cliniccare.dto.MedicalRecordDTO;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.model.DoctorProfile;
import com.example.cliniccare.model.MedicalRecord;
import com.example.cliniccare.model.Service;
import com.example.cliniccare.model.User;
import com.example.cliniccare.repository.DoctorProfileRepository;
import com.example.cliniccare.repository.MedicalRecordRepository;
import com.example.cliniccare.repository.ServiceRepository;
import com.example.cliniccare.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@org.springframework.stereotype.Service
public class MedicalRecordService {
    private static final Logger logger = LoggerFactory.getLogger(MedicalRecordService.class);

    private final MedicalRecordRepository medicalRecordRepository;
    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final ServiceRepository serviceRepository;

    @Autowired
    public MedicalRecordService(
            MedicalRecordRepository medicalRecordRepository,
            UserRepository userRepository,
            DoctorProfileRepository doctorProfileRepository,
            ServiceRepository serviceRepository
    ) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.userRepository = userRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.serviceRepository = serviceRepository;
    }

    public List<MedicalRecordDTO> getMedicalRecord() {
        List<MedicalRecord> medicalRecords = medicalRecordRepository.findByDeleteAtIsNull();

        return medicalRecords
                .stream()
                .map(MedicalRecordDTO :: new)
                .toList();
    }


    public MedicalRecordDTO getMedicalRecordById(UUID id) {
        MedicalRecord medicalRecord = medicalRecordRepository.findByMedicalRecordIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Medical record not found"));

        return new MedicalRecordDTO(medicalRecord);
    }

    public MedicalRecordDTO createMedicalRecord(MedicalRecordDTO medicalRecordDTO) {

        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setMessage(medicalRecordDTO.getMessage());
        medicalRecord.setCreateAt(Timestamp.valueOf(LocalDateTime.now()));
        medicalRecord.setDeleteAt(medicalRecordDTO.getDeleteAt());

        User user = userRepository.findByUserIdAndDeleteAtIsNull(medicalRecordDTO.getPatientId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!user.getRole().getName().equals("User")) {
            throw new IllegalStateException("User must have role 'User' to create a medical record.");
        }
        medicalRecord.setPatient(user);

        DoctorProfile doctorProfile = doctorProfileRepository.findByDoctorProfileIdAndDeleteAtIsNull(medicalRecordDTO.getDoctorProfileId())
                .orElseThrow(() -> new NotFoundException("Doctor profile not found"));
        if (!doctorProfile.getUser().getRole().getName().equals("Doctor")) {
            throw new IllegalStateException("Doctor must have role 'Doctor' to create a medical record.");
        }
        medicalRecord.setDoctor(doctorProfile);

        Service service = serviceRepository.findByServiceId(medicalRecordDTO.getServiceId())
                .orElseThrow(() -> new NotFoundException("Service not found"));
        medicalRecord.setService(service);

        MedicalRecord savedMedicalRecord = medicalRecordRepository.save(medicalRecord);
        return new MedicalRecordDTO(savedMedicalRecord);
    }

    public MedicalRecordDTO updateMedicalRecord(UUID id, MedicalRecordDTO medicalRecordDTO) {
        MedicalRecord medicalRecord = medicalRecordRepository.findByMedicalRecordIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Medical record not found"));

        medicalRecord.setMessage(medicalRecordDTO.getMessage());
        medicalRecord.setCreateAt(medicalRecordDTO.getCreateAt());
        medicalRecord.setDeleteAt(medicalRecordDTO.getDeleteAt());

        User user = userRepository.findByUserIdAndDeleteAtIsNull(medicalRecordDTO.getPatientId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        medicalRecord.setPatient(user);

        DoctorProfile doctorProfile = doctorProfileRepository.findByDoctorProfileIdAndDeleteAtIsNull(medicalRecordDTO.getDoctorProfileId())
                .orElseThrow(() -> new NotFoundException("Doctor profile not found"));
        medicalRecord.setDoctor(doctorProfile);

        Service service = serviceRepository.findServiceId(medicalRecordDTO.getServiceId())
                .orElseThrow(() -> new NotFoundException("Service not found"));
        medicalRecord.setService(service);


        MedicalRecord savedMedicalRecord = medicalRecordRepository.save(medicalRecord);
        return new MedicalRecordDTO(savedMedicalRecord);
    }

    public void deleteMedicalRecord(UUID id) {
        MedicalRecord medicalRecord = medicalRecordRepository.findByMedicalRecordIdAndDeleteAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Medical record not found"));

        medicalRecord.setDeleteAt(new Date());
        medicalRecordRepository.save(medicalRecord);

    }

}
