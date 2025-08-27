package com.uphill.healthcare_booking_system.controller;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uphill.healthcare_booking_system.controller.input.AppointmentInput;
import com.uphill.healthcare_booking_system.controller.output.AppointmentOutput;
import com.uphill.healthcare_booking_system.domain.AppointmentDomain;
import com.uphill.healthcare_booking_system.domain.PatientDomain;
import com.uphill.healthcare_booking_system.service.AppointmentService;

@RestController
@RequestMapping("/v1/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping
    public AppointmentOutput bookAppointment(@RequestBody AppointmentInput appointmentInput) {
        AppointmentDomain appointmentDomain = convertToDomain(appointmentInput);

        AppointmentDomain savedDomain = appointmentService.bookAppointment(appointmentDomain);

        return convertToOutput(savedDomain);
    }

    @GetMapping
    public List<AppointmentOutput> getAllAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return appointmentService.getAllAppointments(page, size).stream()
                .map(this::convertToOutput)
                .collect(Collectors.toList());
    }

    private AppointmentDomain convertToDomain(AppointmentInput appointmentInput) {
        long instantStart = Instant.ofEpochSecond(appointmentInput.getStartDate()).toEpochMilli();
        long instantEnd = Instant.ofEpochSecond(appointmentInput.getEndDate()).toEpochMilli();

        LocalDateTime start = LocalDateTime.ofInstant(Instant.ofEpochMilli(instantStart), ZoneId.systemDefault());
        LocalDateTime end = LocalDateTime.ofInstant(Instant.ofEpochMilli(instantEnd), ZoneId.systemDefault());

        PatientDomain patientDomain = new PatientDomain();
        patientDomain.setName(appointmentInput.getPatientName());
        patientDomain.setEmail(appointmentInput.getPatientEmail());

        AppointmentDomain appointmentDomain = new AppointmentDomain();
        appointmentDomain.setStartTime(start);
        appointmentDomain.setEndTime(end);
        appointmentDomain.setSpecialty(appointmentInput.getSpecialty());
        appointmentDomain.setPatient(patientDomain);

        return appointmentDomain;
    }

    private AppointmentOutput convertToOutput(AppointmentDomain appointmentDomain) {
        AppointmentOutput appointmentOutput = new AppointmentOutput();
        appointmentOutput.setAppointmentId(appointmentDomain.getId());
        appointmentOutput.setStartTime(appointmentDomain.getStartTime());
        appointmentOutput.setEndTime(appointmentDomain.getEndTime());
        appointmentOutput.setDoctorId(appointmentDomain.getDoctor().getId());
        appointmentOutput.setRoomId(appointmentDomain.getRoom().getId());
        appointmentOutput.setStatus(appointmentDomain.getStatus());
        return appointmentOutput;
    }
}
