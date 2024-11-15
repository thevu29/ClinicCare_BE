package com.example.cliniccare.service;

import com.example.cliniccare.dto.PaginationDTO;
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
import com.example.cliniccare.response.PaginationResponse;
import com.example.cliniccare.utils.DateQueryParser;
import com.example.cliniccare.utils.Formatter;
import com.example.cliniccare.utils.TimeQueryParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

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
    private final PaginationService paginationService;

    @Autowired
    public ScheduleService(
            ScheduleRepository scheduleRepository,
            ServiceRepository serviceRepository,
            DoctorProfileRepository doctorProfileRepository,
            PaginationService paginationService
    ) {
        this.scheduleRepository = scheduleRepository;
        this.serviceRepository = serviceRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.paginationService = paginationService;
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

    public PaginationResponse<List<ScheduleDTO>> getSchedules(
            PaginationDTO paginationDTO, String search, String date, String time,
            String status, UUID serviceId, UUID doctorId
    ) {
        Pageable pageable = paginationService.getPageable(paginationDTO);

        Specification<Schedule> spec = Specification.where(null);

        if (search != null && !search.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(root.get("doctor").get("user").get("name"), "%" + search + "%"),
                    cb.like(root.get("service").get("name"), "%" + search + "%")
            ));
        }
        if (date != null && !date.isEmpty()) {
            DateQueryParser<Schedule> dateQueryParser = new DateQueryParser<>(date, "dateTime");
            spec = spec.and(dateQueryParser.createDateSpecification());
        }
        if (time != null && !time.isEmpty()) {
            TimeQueryParser<Schedule> timeQueryParser = new TimeQueryParser<>(time, "dateTime");
            spec = spec.and(timeQueryParser.createTimeSpecification());
        }
        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), getScheduleStatus(status)));
        }
        if (serviceId != null) {
            Service service = serviceRepository.findById(serviceId)
                    .orElseThrow(() -> new NotFoundException("Service not found"));

            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("service").get("serviceId"), service.getServiceId()));
        }
        if (doctorId != null) {
            DoctorProfile doctor = doctorProfileRepository.findById(doctorId)
                    .orElseThrow(() -> new NotFoundException("Doctor not found"));

            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("doctor").get("doctorProfileId"), doctor.getDoctorProfileId()));
        }

        Page<Schedule> schedules = scheduleRepository.findAll(spec, pageable);

        int totalPages = schedules.getTotalPages();
        long totalElements = schedules.getTotalElements();
        int take = schedules.getNumberOfElements();

        Map<String, List<Schedule>> groupedSchedules = schedules.stream()
                .collect(Collectors.groupingBy(schedule -> schedule.getDoctor().getDoctorProfileId() + "-" +
                        schedule.getService().getServiceId() + "-" + schedule.getDateTime().toLocalDate()));

        List<ScheduleDTO> scheduleDTOS = groupedSchedules.values().stream()
                .map(ScheduleDTO::new)
                .toList();

        return new PaginationResponse<>(
                true,
                "Get schedules successfully",
                scheduleDTOS,
                paginationDTO.page,
                paginationDTO.size,
                take,
                totalPages,
                totalElements
        );
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
