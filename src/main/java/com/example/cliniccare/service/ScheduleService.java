package com.example.cliniccare.service;

import com.example.cliniccare.dto.*;
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

    public PaginationResponse<List<SchedulesDTO>> getSchedules(
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

        List<SchedulesDTO> scheduleDTOS = groupedSchedules.values().stream()
                .map(scheduleList -> {
                    List<Schedule> sortedSchedules = scheduleList.stream()
                            .sorted(Comparator.comparing(Schedule::getDateTime))
                            .collect(Collectors.toList());
                    return new SchedulesDTO(sortedSchedules);
                })
                .sorted((dto1, dto2) -> {
                    LocalDate date1 = dto1.getDate();
                    LocalDate date2 = dto2.getDate();

                    if (date1.equals(date2) && !dto1.getScheduleDetails().isEmpty() && !dto2.getScheduleDetails().isEmpty()) {
                        LocalTime time1 = LocalTime.parse(dto1.getScheduleDetails().getFirst().getTime());
                        LocalTime time2 = LocalTime.parse(dto2.getScheduleDetails().getFirst().getTime());
                        return paginationDTO.order.equalsIgnoreCase("desc") ?
                                time2.compareTo(time1) : time1.compareTo(time2);
                    }

                    return paginationDTO.order.equalsIgnoreCase("desc") ?
                            date2.compareTo(date1) : date1.compareTo(date2);
                })
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
        if (!validateConflict(scheduleDTO.getDate(), scheduleDTO.getTime(), scheduleDTO.getDuration())) {
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
        schedule.setDateTime(scheduleDTO.getDate().atTime(scheduleDTO.getTime()));
        schedule.setDuration(scheduleDTO.getDuration());
        schedule.setStatus(getScheduleStatus(scheduleDTO.getStatus()));

        scheduleRepository.save(schedule);

        return new ScheduleDTO(schedule);
    }

    public List<SchedulesDTO> autoCreateSchedules(ScheduleFormDTO scheduleDTO) {
        if (scheduleDTO.getDates().length == 0) {
            throw new BadRequestException("Dates are required");
        }

        Arrays.stream(scheduleDTO.getDates()).forEach(date -> {
            if (!Validation.isValidDate(String.valueOf(date))) {
                throw new BadRequestException("The date must be in format yyyy-MM-dd (e.g., 2024-12-31)");
            }
        });

        Service service = serviceRepository.findById(scheduleDTO.getServiceId())
                .orElseThrow(() -> new NotFoundException("Service not found"));

        DoctorProfile doctorProfile = doctorProfileRepository.findById(scheduleDTO.getDoctorProfileId())
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        List<SchedulesDTO> result = new ArrayList<>();
        LocalTime workingStart = LocalTime.of(8, 0);
        LocalTime workingEnd = LocalTime.of(23, 0);
        int schedulesPerDay = (int) Math.ceil((double) scheduleDTO.getAmount() / scheduleDTO.getDates().length);
        int remainingSchedules = scheduleDTO.getAmount();

        for (LocalDate date : scheduleDTO.getDates()) {
            SchedulesDTO dateSchedulesDto = new SchedulesDTO();
            dateSchedulesDto.setDate(date);
            dateSchedulesDto.setServiceId(service.getServiceId());
            dateSchedulesDto.setServiceName(service.getName());
            dateSchedulesDto.setDoctorProfileId(doctorProfile.getDoctorProfileId());
            dateSchedulesDto.setDoctorName(doctorProfile.getUser().getName());
            dateSchedulesDto.setScheduleDetails(new ArrayList<>());

            List<Schedule> existingSchedules = scheduleRepository
                    .findByDateTimeBetween(date.atStartOfDay(), date.plusDays(1).atStartOfDay());

            List<TimeSlotDTO> busySlots = existingSchedules.stream()
                    .map(s -> new TimeSlotDTO(
                            LocalTime.from(s.getDateTime()),
                            LocalTime.from(s.getDateTime().plusMinutes(s.getDuration()))
                    ))
                    .collect(Collectors.toList());

            int schedulesToCreate = Math.min(schedulesPerDay, remainingSchedules);

            List<TimeSlotDTO> availableSlots = getOptimalTimeSlots(
                    workingStart,
                    workingEnd,
                    busySlots,
                    scheduleDTO.getDuration(),
                    schedulesToCreate
            );

            if (availableSlots.size() < schedulesToCreate) {
                throw new BadRequestException("Cannot create " + schedulesToCreate + " schedules for date "
                        + date + ". Only " + availableSlots.size() + " slots are available.");
            }

            for (TimeSlotDTO slot : availableSlots) {
                LocalDateTime scheduleDate = date.atTime(slot.getStart());

                Schedule schedule = new Schedule();
                schedule.setService(service);
                schedule.setDoctor(doctorProfile);
                schedule.setDateTime(scheduleDate);
                schedule.setDuration(scheduleDTO.getDuration());
                schedule.setStatus(Schedule.ScheduleStatus.valueOf(scheduleDTO.getStatus().toUpperCase()));

                Schedule createdSchedule = scheduleRepository.save(schedule);

                ScheduleDetailDTO scheduleDetailDto = new ScheduleDetailDTO();
                scheduleDetailDto.setScheduleId(createdSchedule.getScheduleId());
                scheduleDetailDto.setTime(Formatter.formatTime(createdSchedule.getDateTime()));
                scheduleDetailDto.setDuration(createdSchedule.getDuration());
                scheduleDetailDto.setStatus(createdSchedule.getStatus().name());

                dateSchedulesDto.getScheduleDetails().add(scheduleDetailDto);
            }

            result.add(dateSchedulesDto);
            remainingSchedules -= dateSchedulesDto.getScheduleDetails().size();
        }

        return result;
    }

    private List<TimeSlotDTO> getOptimalTimeSlots(
            LocalTime workingStart,
            LocalTime workingEnd,
            List<TimeSlotDTO> busySlots,
            int duration,
            int requiredSlots
    ) {
        List<TimeSlotDTO> result = new ArrayList<>();
        LocalTime currentTime = workingStart;

        while (currentTime.plusMinutes(duration).isBefore(workingEnd) && result.size() < requiredSlots) {
            TimeSlotDTO potentialSlot = new TimeSlotDTO(currentTime, currentTime.plusMinutes(duration));

            if (isSlotAvailable(potentialSlot, busySlots)) {
                result.add(potentialSlot);
                currentTime = currentTime.plusMinutes(duration + 15);
            } else {
                currentTime = currentTime.plusMinutes(15);
            }
        }

        return result;
    }

    private boolean isSlotAvailable(TimeSlotDTO newSlot, List<TimeSlotDTO> busySlots) {
        for (TimeSlotDTO busySlot : busySlots) {
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

        LocalDate date = scheduleDTO.getDate() != null && !String.valueOf(scheduleDTO.getDate()).isEmpty()
                ? scheduleDTO.getDate()
                : schedule.getDateTime().toLocalDate();

        LocalTime time = scheduleDTO.getTime() != null && !String.valueOf(scheduleDTO.getTime()).isEmpty()
                ? scheduleDTO.getTime()
                : schedule.getDateTime().toLocalTime();

        LocalDateTime dateTime = date.atTime(time);

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
