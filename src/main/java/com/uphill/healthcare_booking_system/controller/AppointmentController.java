package com.uphill.healthcare_booking_system.controller;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uphill.healthcare_booking_system.controller.input.AppointmentInput;
import com.uphill.healthcare_booking_system.controller.output.AppointmentOutput;
import com.uphill.healthcare_booking_system.repository.entity.Appointment;
import com.uphill.healthcare_booking_system.service.AppointmentService;

@RestController
@RequestMapping("/v1/appointments")
public class AppointmentController {
    
    @Autowired 
    private AppointmentService appointmentService;

    @PostMapping
    public AppointmentOutput bookAppointment(@RequestBody AppointmentInput appointmentInput) {
        // Usually, for changing between object layers, I create an adapter class to convert input -> domain <-> database object and domain -> output.
        // As this is a simple method, I will just convert the input to a domain object and pass it to the service and than convert the domain to a output object.
        
        long instantStart = Instant.ofEpochSecond(appointmentInput.getStartDate()).toEpochMilli();
        long instantEnd = Instant.ofEpochSecond(appointmentInput.getEndDate()).toEpochMilli();

        LocalDateTime start = LocalDateTime.ofInstant(Instant.ofEpochMilli(instantStart), ZoneId.systemDefault());
        LocalDateTime end = LocalDateTime.ofInstant(Instant.ofEpochMilli(instantEnd), ZoneId.systemDefault());
        
        Appointment appointment = new Appointment();        
        appointment.setStartTime(start);
        appointment.setEndTime(end);
        appointment = appointmentService.bookAppointment(appointment);

        AppointmentOutput appointmentOutput = new AppointmentOutput();
        appointmentOutput.setAppointmentId(appointment.getId());
        appointmentOutput.setStartTime(appointment.getStartTime());
        appointmentOutput.setEndTime(appointment.getEndTime());
        appointmentOutput.setDoctorId(appointment.getDoctor().getId());
        appointmentOutput.setRoomId(appointment.getRoom().getId());
        return appointmentOutput;
    }
}
