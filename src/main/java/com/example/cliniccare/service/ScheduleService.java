package com.example.cliniccare.service;

import com.example.cliniccare.dto.ScheduleDTO;
import com.example.cliniccare.dto.ScheduleFormDTO;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.model.DoctorProfile;
import com.example.cliniccare.model.Schedule;
import com.example.cliniccare.model.Service;
import com.example.cliniccare.repository.DoctorProfileRepository;
import com.example.cliniccare.repository.ScheduleRepository;
import com.example.cliniccare.repository.ServiceRepository;
import com.example.cliniccare.utils.Formatter;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@org.springframework.stereotype.Service
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final ServiceRepository serviceRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    @Autowired
    public ScheduleService(
            ScheduleRepository scheduleRepository,
            ServiceRepository serviceRepository,
            DoctorProfileRepository doctorProfileRepository
    ) {
        this.scheduleRepository = scheduleRepository;
        this.serviceRepository = serviceRepository;
        this.doctorProfileRepository = doctorProfileRepository;
    }

    private Schedule.ScheduleStatus getScheduleStatus(String status) {
        try {
            return status != null && !status.isEmpty()
                    ? Schedule.ScheduleStatus.valueOf(status.toUpperCase())
                    : Schedule.ScheduleStatus.AVAILABLE;
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Invalid status");
        }
    }

    private Boolean validateConflict(LocalDate date, LocalTime time, Integer duration) {
        LocalDateTime startDateTime = date.atTime(time);
        LocalDateTime endDateTime = startDateTime.plusMinutes(duration);

        List<Schedule> conflictingSchedules = scheduleRepository
                .findByDateTimeBetween(startDateTime, endDateTime);

        if (!conflictingSchedules.isEmpty()) {
            Schedule conflictingSchedule = conflictingSchedules.getFirst();
            String conflictingStartTime = Formatter.formatTime(conflictingSchedule.getDateTime());
            String conflictingEndTime = Formatter.formatTime(
                    conflictingSchedule.getDateTime().plusMinutes(conflictingSchedule.getDuration()));

            throw new BadRequestException(
                    "There is already a schedule from " + conflictingStartTime + " to " + conflictingEndTime +
                            " on " + conflictingSchedule.getDateTime().toLocalDate()
            );
        }
        return true;
    }

    public List<ScheduleDTO> getSchedules() {
        List<Schedule> schedules = scheduleRepository.findAllByOrderByDateTimeAsc();

        Map<String, List<Schedule>> groupedSchedules = schedules.stream()
                .collect(Collectors.groupingBy(schedule -> schedule.getDoctor().getDoctorProfileId() + "-" +
                        schedule.getService().getServiceId() + "-" + schedule.getDateTime().toLocalDate()));

        return groupedSchedules.values().stream()
                .map(ScheduleDTO::new)
                .collect(Collectors.toList());
    }

    public List<ScheduleDTO> getDoctorSchedules(UUID doctorProfileId) {
        DoctorProfile doctor = doctorProfileRepository
                .findByDoctorProfileIdAndDeleteAtIsNull(doctorProfileId)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        List<Schedule> schedules = scheduleRepository
                .findAllByDoctor_DoctorProfileIdOrderByDateTimeAsc(doctor.getDoctorProfileId());

        Map<String, List<Schedule>> groupedSchedules = schedules.stream()
                .collect(Collectors.groupingBy(schedule -> schedule.getService().getServiceId() + "-" +
                        schedule.getDateTime().toLocalDate()));

        return groupedSchedules.values().stream()
                .map(ScheduleDTO::new)
                .collect(Collectors.toList());
    }

    public List<ScheduleDTO> getServiceSchedules(UUID  serviceId) {
        Service service = serviceRepository
                .findByServiceIdAndDeleteAtIsNull(serviceId)
                .orElseThrow(() -> new NotFoundException("Service not found"));

        List<Schedule> schedules = scheduleRepository
                .findAllByService_ServiceIdOrderByDateTimeAsc(service.getServiceId());

        Map<String, List<Schedule>> groupedSchedules = schedules.stream()
                .collect(Collectors.groupingBy(schedule -> schedule.getDoctor().getDoctorProfileId() + "-" +
                        schedule.getDateTime().toLocalDate()));

        return groupedSchedules.values().stream()
                .map(ScheduleDTO::new)
                .collect(Collectors.toList());
    }

    public ScheduleDTO createSchedule(ScheduleFormDTO scheduleDTO) {
        if (scheduleDTO.getTimes().length == 0) {
            throw new BadRequestException("Times array cannot be empty");
        }
        if (scheduleDTO.getDurations().length == 0) {
            throw new BadRequestException("Durations array cannot be empty");
        }
        if (scheduleDTO.getTimes().length != scheduleDTO.getDurations().length) {
            throw new BadRequestException("Times and durations arrays must have the same length");
        }

        Service service = serviceRepository
                .findById(scheduleDTO.getServiceId())
                .orElseThrow(() -> new NotFoundException("Service not found"));

        if (service.getStatus() != Service.ServiceStatus.AVAILABLE) {
            throw new BadRequestException("Service is unavailable");
        }

        DoctorProfile doctorProfile = doctorProfileRepository
                .findById(scheduleDTO.getDoctorProfileId())
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        List<Schedule> schedules = IntStream.range(0, scheduleDTO.getTimes().length)
                .mapToObj(i -> {
                    LocalTime parsedTime = Formatter.parseTime(scheduleDTO.getTimes()[i]);

                    if (!validateConflict(scheduleDTO.getDate(), parsedTime, scheduleDTO.getDurations()[i])) {
                        return null;
                    }

                    Schedule schedule = new Schedule();
                    schedule.setService(service);
                    schedule.setDoctor(doctorProfile);
                    schedule.setDateTime(scheduleDTO.getDate().atTime(parsedTime));
                    schedule.setDuration(scheduleDTO.getDurations()[i]);
                    schedule.setStatus(getScheduleStatus(scheduleDTO.getStatus()));

                    return schedule;
                })
                .collect(Collectors.toList());

        scheduleRepository.saveAll(schedules);

        return new ScheduleDTO(schedules);
    }

    public ScheduleDTO updateSchedule(UUID id, ScheduleFormDTO scheduleDTO) {
        Schedule schedule = scheduleRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Schedule not found"));

        if (schedule.getAppointment() != null) {
            throw new BadRequestException("Cannot update schedule with appointment");
        }

        if (scheduleDTO.getDuration() != null) {
            schedule.setDuration(scheduleDTO.getDuration());
        }

        LocalDate date = scheduleDTO.getDate() != null && !String.valueOf(scheduleDTO.getDate()).isEmpty()
                ? scheduleDTO.getDate()
                : schedule.getDateTime().toLocalDate();

        LocalTime time = scheduleDTO.getTime() != null && !scheduleDTO.getTime().isEmpty()
                ? Formatter.parseTime(scheduleDTO.getTime())
                : schedule.getDateTime().toLocalTime();

        LocalDateTime dateTime = date.atTime(time);

        if (!schedule.getDateTime().equals(dateTime)) {
            if (!validateConflict(date, time, schedule.getDuration())) {
                return null;
            }
        }
        schedule.setDateTime(dateTime);

        if (scheduleDTO.getStatus() != null && !scheduleDTO.getStatus().isEmpty()) {
            schedule.setStatus(getScheduleStatus(scheduleDTO.getStatus()));
        }

        scheduleRepository.save(schedule);

        return new ScheduleDTO(schedule);
    }
}
