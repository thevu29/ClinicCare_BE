package com.example.cliniccare.service;

import com.example.cliniccare.dto.AppointmentDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.model.*;
import com.example.cliniccare.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    @Autowired
    public AppointmentService(
            AppointmentRepository appointmentRepository,
            ScheduleRepository scheduleRepository,
            UserRepository userRepository,
            NotificationRepository notificationRepository,
            DoctorProfileRepository doctorProfileRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.doctorProfileRepository = doctorProfileRepository;
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

    public List<AppointmentDTO> getAppointments() {
        List<Appointment> appointments = appointmentRepository.findAll();
        return appointments.stream().map(AppointmentDTO::new).toList();
    }

    public List<AppointmentDTO> getPatientAppointments(UUID patientId) {
        User patient = userRepository.findByUserIdAndDeleteAtIsNull(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found"));

        List<Appointment> appointments = appointmentRepository.findByPatientUserId(patient.getUserId());
        return appointments.stream().map(AppointmentDTO::new).toList();
    }

    public List<AppointmentDTO> getDoctorAppointments(UUID doctorId) {
        DoctorProfile doctor = doctorProfileRepository.findByDoctorProfileIdAndDeleteAtIsNull(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        List<Appointment> appointments = appointmentRepository
                .findByScheduleDoctorDoctorProfileId(doctor.getDoctorProfileId());
        return appointments.stream().map(AppointmentDTO::new).toList();
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
        schedule.setStatus(Schedule.ScheduleStatus.AVAILABLE);
        scheduleRepository.save(schedule);

        User userReceiveNotification = determineNotificationRecipient(appointmentDTO.getCancelBy(), appointment, schedule);
        String message = generateCancellationMessage(appointmentDTO.getCancelBy(), appointment, schedule);

        createNotification(message, userReceiveNotification);

        return new AppointmentDTO(appointment);
    }
}
