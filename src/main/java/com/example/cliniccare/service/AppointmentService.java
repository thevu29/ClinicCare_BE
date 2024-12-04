package com.example.cliniccare.service;

import com.example.cliniccare.dto.AppointmentDTO;
import com.example.cliniccare.dto.PaginationDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.entity.*;
import com.example.cliniccare.repository.*;
import com.example.cliniccare.response.PaginationResponse;
import com.example.cliniccare.utils.DateQueryParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PaginationService paginationService;

    @Autowired
    public AppointmentService(
            AppointmentRepository appointmentRepository,
            ScheduleRepository scheduleRepository,
            UserRepository userRepository,
            NotificationRepository notificationRepository,
            DoctorProfileRepository doctorProfileRepository,
            PaginationService paginationService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.paginationService = paginationService;
    }

    private void createNotification(String message, User user) {
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setUser(user);
        notificationRepository.save(notification);
    }

    private User determineNotificationRecipient(UUID userId, Appointment appointment, Schedule schedule) {
        if (userId.equals(appointment.getPatient().getUserId())) {
            return schedule.getDoctor().getUser();
        } else {
            return appointment.getPatient();
        }
    }

    private String generateCancellationMessage(UUID userId, Appointment appointment, Schedule schedule) {
        if (userId.equals(appointment.getPatient().getUserId())) {
            return "Your appointment at " + schedule.getDateTime().toLocalTime() + " on " +
                    schedule.getDateTime().toLocalDate() + " has been cancelled by patient";
        } else {
            return "Your appointment at " + schedule.getDateTime().toLocalTime() + " on " +
                    schedule.getDateTime().toLocalDate() + " has been cancelled by doctor";
        }
    }

    public List<AppointmentDTO> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(AppointmentDTO::new)
                .collect(Collectors.toList());
    }

    public PaginationResponse<List<AppointmentDTO>> getAppointments(
            PaginationDTO paginationDTO,
            String search,
            String date,
            String status,
            UUID patientId,
            UUID userId
    ) {
        Pageable pageable = paginationService.getPageable(paginationDTO);

        Specification<Appointment> spec = Specification.where(null);

        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(root.get("patientName"), "%" + search + "%"),
                            cb.like(root.get("patientPhone"), "%" + search + "%"),
                            cb.like(root.get("cancelReason"), "%" + search + "%"),
                            cb.like(root.get("schedule").get("service").get("name"), "%" + search + "%")
                    )
            );
        }
        if (date != null && !date.trim().isEmpty()) {
            DateQueryParser<Appointment> dateParser = new DateQueryParser<>(date, "date");
            Specification<Appointment> dateSpec = dateParser.createDateSpecification();
            spec = spec.and(dateSpec);
        }
        if (status != null && !status.trim().isEmpty()) {
            String[] allowedStatus = {"active", "cancelled", "completed"};

            if (!Arrays.asList(allowedStatus).contains(status.toLowerCase())) {
                throw new BadRequestException("Invalid status (only 'Active', 'Cancelled', or 'Completed' allowed)");
            }

            spec = switch (status.toLowerCase()) {
                case "active" -> spec.and((root, query, cb) -> cb.and(
                        cb.isNull(root.get("cancelBy")),
                        cb.notEqual(root.get("schedule").get("status"), Schedule.ScheduleStatus.COMPLETED)
                ));
                case "cancelled" -> spec.and((root, query, cb) -> cb.and(
                        cb.isNotNull(root.get("cancelBy")),
                        cb.notEqual(root.get("schedule").get("status"), Schedule.ScheduleStatus.COMPLETED)
                ));
                case "completed" -> spec.and((root, query, cb)
                        -> cb.equal(root.get("schedule").get("status"), Schedule.ScheduleStatus.COMPLETED));
                default -> spec;
            };
        }
        if (patientId != null) {
            User patient = userRepository.findByUserIdAndDeleteAtIsNull(patientId)
                    .orElseThrow(() -> new NotFoundException("Patient not found"));

            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("patient").get("userId"), patient.getUserId()));
        }
        if (userId != null) {
            DoctorProfile doctor = doctorProfileRepository.findByUser_UserIdAndDeleteAtIsNull(userId)
                    .orElseThrow(() -> new NotFoundException("Doctor not found"));

            spec = spec.and((root, query, cb)
                    -> cb.equal(root.get("schedule").get("doctor").get("doctorProfileId"), doctor.getDoctorProfileId()));
        }

        Page<Appointment> appointments = appointmentRepository.findAll(spec, pageable);

        int totalPages = paginationService.getTotalPages(appointments.getTotalElements(), paginationDTO.size);
        long totalElements = appointments.getTotalElements();
        int take = appointments.getNumberOfElements();

        return new PaginationResponse<>(
                true,
                "Get appointments successfully",
                appointments.map(AppointmentDTO::new).getContent(),
                paginationDTO.page,
                paginationDTO.size,
                take,
                totalPages,
                totalElements
        );
    }

    public AppointmentDTO getAppointmentById(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));

        return new AppointmentDTO(appointment);
    }

    public AppointmentDTO createAppointment(AppointmentDTO appointmentDTO) {
        User patient = userRepository.findByUserIdAndDeleteAtIsNull(appointmentDTO.getPatientId())
                .orElseThrow(() -> new NotFoundException("Patient not found"));

        Schedule schedule = scheduleRepository.findById(appointmentDTO.getScheduleId())
                .orElseThrow(() -> new NotFoundException("Schedule not found"));

        if (schedule.getStatus() == Schedule.ScheduleStatus.UNAVAILABLE) {
            throw new BadRequestException("Schedule is unavailable");
        }
        if (schedule.getStatus() == Schedule.ScheduleStatus.BOOKED) {
            throw new BadRequestException("Schedule is already booked");
        }
        if (schedule.getDateTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Schedule is already passed");
        }

        schedule.setStatus(Schedule.ScheduleStatus.BOOKED);
        scheduleRepository.save(schedule);

        Appointment appointment = new Appointment();
        appointment.setPatientName(appointmentDTO.getPatientName());
        appointment.setPatientPhone(appointmentDTO.getPatientPhone());
        appointment.setSchedule(schedule);
        appointment.setPatient(patient);
        appointmentRepository.save(appointment);

        createNotification(
                "Schedule at " + schedule.getDateTime().toLocalTime() + " on " +
                        schedule.getDateTime().toLocalDate()  + " has been booked",
                schedule.getDoctor().getUser()
        );

        return new AppointmentDTO(appointment);
    }

    public AppointmentDTO cancelAppointment(UUID appointmentId, AppointmentDTO appointmentDTO) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));

        if (appointment.getCancelBy() != null) {
            throw new BadRequestException("Appointment is already cancelled");
        }

        User userCancel = userRepository.findByUserIdAndDeleteAtIsNull(appointmentDTO.getCancelBy())
                .orElseThrow(() -> new NotFoundException("User not found"));

        appointment.setCancelBy(userCancel);
        appointment.setCancelAt(LocalDateTime.now());
        appointment.setCancelReason(appointmentDTO.getCancelReason());
        appointmentRepository.save(appointment);

        Schedule schedule = appointment.getSchedule();

        if (schedule.getStatus() == Schedule.ScheduleStatus.COMPLETED) {
            throw new BadRequestException("Appointment is already completed");
        }

        schedule.setStatus(Schedule.ScheduleStatus.AVAILABLE);
        scheduleRepository.save(schedule);

        User userReceiveNotification = determineNotificationRecipient(appointmentDTO.getCancelBy(), appointment, schedule);
        String message = generateCancellationMessage(appointmentDTO.getCancelBy(), appointment, schedule);

        createNotification(message, userReceiveNotification);

        return new AppointmentDTO(appointment);
    }

    public AppointmentDTO completeAppointment(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));

        if (appointment.getCancelBy() != null) {
            throw new BadRequestException("Appointment is already cancelled");
        }

        Schedule schedule = appointment.getSchedule();

        if (schedule.getDateTime().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Appointment is not yet due");
        }

        schedule.setStatus(Schedule.ScheduleStatus.COMPLETED);

        scheduleRepository.save(schedule);
        appointmentRepository.save(appointment);

        return new AppointmentDTO(appointment);
    }

    public long getAppointmentCountForMonth(Integer month, Integer year) {
        if (month == null || year == null) {
            throw new BadRequestException("Please provide month and year");
        }
        if (month < 1 || month > 12) {
            throw new BadRequestException("Invalid month. Please provide a value between 1 and 12.");
        }

        return appointmentRepository.countAppointmentsByMonth(month, year);
    }
}
