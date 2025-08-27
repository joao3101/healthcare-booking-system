package com.uphill.healthcare_booking_system.controller;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uphill.healthcare_booking_system.controller.input.AppointmentInput;
import com.uphill.healthcare_booking_system.controller.output.AppointmentOutput;
import com.uphill.healthcare_booking_system.domain.AppointmentDomain;
import com.uphill.healthcare_booking_system.domain.PatientDomain;
import com.uphill.healthcare_booking_system.service.AppointmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping
    public AppointmentOutput bookAppointment(@Valid @RequestBody AppointmentInput appointmentInput) {
        AppointmentDomain appointmentDomain = convertToDomain(appointmentInput);

        AppointmentDomain savedDomain = appointmentService.bookAppointment(appointmentDomain);

        return convertToOutput(savedDomain);
    }

    @GetMapping
    public ResponseEntity<Page<AppointmentOutput>> getAllAppointments(
            @PageableDefault(size = 10, sort = "startTime") Pageable pageable) {

        Page<AppointmentDomain> page = appointmentService.getAllAppointments(pageable);
        return ResponseEntity.ok(page.map(this::convertToOutput));
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
