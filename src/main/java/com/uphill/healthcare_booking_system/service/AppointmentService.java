package com.uphill.healthcare_booking_system.service;

import java.time.LocalDateTime;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

import com.uphill.healthcare_booking_system.repository.AppointmentRepository;
import com.uphill.healthcare_booking_system.repository.entity.Appointment;
import com.uphill.healthcare_booking_system.repository.entity.Doctor;
import com.uphill.healthcare_booking_system.repository.entity.Room;

import jakarta.transaction.Transactional;

public class AppointmentService {
    @Autowired
    private AppointmentRepository appointmentRepository;

    private boolean isDoctorAvailable(Doctor doctor, LocalDateTime start, LocalDateTime end) {
        List<Appointment> overlapping = appointmentRepository
                .findByDoctorAndStartTimeLessThanAndEndTimeGreaterThan(doctor, end, start);
        return overlapping.isEmpty();
    }

    private boolean isRoomAvailable(Room room, LocalDateTime start, LocalDateTime end) {
        List<Appointment> overlapping = appointmentRepository
                .findByRoomAndStartTimeLessThanAndEndTimeGreaterThan(room, end, start);
        return overlapping.isEmpty();
    }

    @Transactional
    public Appointment bookAppointment(Appointment appointment) {
        if (!isDoctorAvailable(appointment.getDoctor(), appointment.getStartTime(), appointment.getEndTime())) {
            throw new IllegalArgumentException("Doctor is not available at this time");
        }
        if (!isRoomAvailable(appointment.getRoom(), appointment.getStartTime(), appointment.getEndTime())) {
            throw new IllegalArgumentException("Room is not available at this time");
        }
        return appointmentRepository.save(appointment);
    }
}
