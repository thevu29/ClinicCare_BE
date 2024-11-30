package com.example.cliniccare.service;

import com.example.cliniccare.dto.*;
import com.example.cliniccare.entity.TimeSlot;
import com.example.cliniccare.exception.BadRequestException;
import com.example.cliniccare.exception.NotFoundException;
import com.example.cliniccare.entity.DoctorProfile;
import com.example.cliniccare.entity.Schedule;
import com.example.cliniccare.entity.Service;
import com.example.cliniccare.repository.DoctorProfileRepository;
import com.example.cliniccare.repository.ScheduleRepository;
import com.example.cliniccare.repository.ServiceRepository;
import com.example.cliniccare.response.PaginationResponse;
import com.example.cliniccare.utils.DateQueryParser;
import com.example.cliniccare.utils.Formatter;
import com.example.cliniccare.utils.TimeQueryParser;
import com.example.cliniccare.validation.Validation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

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

    public List<ScheduleDTO> getAllSchedules() {
        List<Schedule> schedules = scheduleRepository.findAllByOrderByDateTimeDesc();
        return schedules.stream().map(ScheduleDTO::new).collect(Collectors.toList());
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

        List<ScheduleDTO> scheduleDTOS = schedules
                .stream()
                .map(ScheduleDTO::new)
                .collect(Collectors.toList());

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

    public ScheduleDTO getScheduleById(UUID id) {
        Schedule schedule = scheduleRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Schedule not found"));

        return new ScheduleDTO(schedule);
    }

    public ScheduleDTO createSchedule(ScheduleFormDTO scheduleDTO) {
        LocalDate date = scheduleDTO.getDateTime().toLocalDate();
        LocalTime time = scheduleDTO.getDateTime().toLocalTime();

        if (!validateConflict(date, time, scheduleDTO.getDuration())) {
            return null;
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

        Schedule schedule = new Schedule();
        schedule.setService(service);
        schedule.setDoctor(doctorProfile);
        schedule.setDateTime(scheduleDTO.getDateTime());
        schedule.setDuration(scheduleDTO.getDuration());
        schedule.setStatus(getScheduleStatus(scheduleDTO.getStatus()));

        scheduleRepository.save(schedule);

        return new ScheduleDTO(schedule);
    }

    public List<ScheduleDTO> autoCreateSchedules(ScheduleFormDTO scheduleRequest) {
        if (scheduleRequest.getDates().length == 0) {
            throw new BadRequestException("Dates are required");
        }

        Arrays.stream(scheduleRequest.getDates()).forEach(date -> {
            if (Validation.isNotValidDate(String.valueOf(date))) {
                throw new BadRequestException("The date must be in format yyyy-MM-dd (e.g., 2024-12-31)");
            }
        });

        Service service = serviceRepository.findById(scheduleRequest.getServiceId())
                .orElseThrow(() -> new NotFoundException("Service not found"));

        DoctorProfile doctorProfile = doctorProfileRepository.findById(scheduleRequest.getDoctorProfileId())
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        List<ScheduleDTO> result = new ArrayList<>();
        LocalTime workingStart = LocalTime.of(8, 0);
        LocalTime workingEnd = LocalTime.of(23, 0);
        int schedulesPerDay = (int) Math.ceil((double) scheduleRequest.getAmount() / scheduleRequest.getDates().length);
        int remainingSchedules = scheduleRequest.getAmount();

        for (LocalDate date : scheduleRequest.getDates()) {
            List<Schedule> existingSchedules = scheduleRepository
                    .findByDateTimeBetween(date.atStartOfDay(), date.plusDays(1).atStartOfDay());

            List<TimeSlot> busySlots = existingSchedules.stream()
                    .map(s -> new TimeSlot(
                            LocalTime.from(s.getDateTime()),
                            LocalTime.from(s.getDateTime().plusMinutes(s.getDuration()))
                    ))
                    .collect(Collectors.toList());

            int schedulesToCreate = Math.min(schedulesPerDay, remainingSchedules);

            List<TimeSlot> availableSlots = getOptimalTimeSlots(
                    workingStart,
                    workingEnd,
                    busySlots,
                    scheduleRequest.getDuration(),
                    schedulesToCreate
            );

            if (availableSlots.size() < schedulesToCreate) {
                throw new BadRequestException("Cannot create " + schedulesToCreate + " schedules for date "
                        + date + ". Only " + availableSlots.size() + " slots are available.");
            }

            for (TimeSlot slot : availableSlots) {
                LocalDateTime scheduleDate = date.atTime(slot.getStart());

                Schedule schedule = new Schedule();
                schedule.setService(service);
                schedule.setDoctor(doctorProfile);
                schedule.setDateTime(scheduleDate);
                schedule.setDuration(scheduleRequest.getDuration());
                schedule.setStatus(Schedule.ScheduleStatus.valueOf(scheduleRequest.getStatus().toUpperCase()));

                Schedule createdSchedule = scheduleRepository.save(schedule);

                ScheduleDTO scheduleDTO = new ScheduleDTO(createdSchedule);
                result.add(scheduleDTO);
            }

            remainingSchedules -= schedulesToCreate;
        }

        return result;
    }

    private List<TimeSlot> getOptimalTimeSlots(
            LocalTime workingStart,
            LocalTime workingEnd,
            List<TimeSlot> busySlots,
            int duration,
            int requiredSlots
    ) {
        List<TimeSlot> result = new ArrayList<>();
        LocalTime currentTime = workingStart;

        while (currentTime.plusMinutes(duration).isBefore(workingEnd) && result.size() < requiredSlots) {
            TimeSlot potentialSlot = new TimeSlot(currentTime, currentTime.plusMinutes(duration));

            if (isSlotAvailable(potentialSlot, busySlots)) {
                result.add(potentialSlot);
                currentTime = currentTime.plusMinutes(duration + 15);
            } else {
                currentTime = currentTime.plusMinutes(15);
            }
        }

        return result;
    }

    private boolean isSlotAvailable(TimeSlot newSlot, List<TimeSlot> busySlots) {
        for (TimeSlot busySlot : busySlots) {
            if (newSlot.getStart().isBefore(busySlot.getEnd()) && busySlot.getStart().isBefore(newSlot.getEnd())) {
                return false;
            }
        }
        return true;
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

        LocalDateTime dateTime = scheduleDTO.getDateTime();
        LocalDate date = dateTime.toLocalDate();
        LocalTime time = dateTime.toLocalTime();

        if (!schedule.getDateTime().equals(dateTime)) {
            if (!validateConflict(date, time, schedule.getDuration())) {
                throw new BadRequestException("There is a conflict with another schedule");
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
