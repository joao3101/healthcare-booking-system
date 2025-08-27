package com.uphill.healthcare_booking_system.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.uphill.healthcare_booking_system.domain.exceptions.NoAvailableDoctorException;
import com.uphill.healthcare_booking_system.repository.AppointmentRepository;
import com.uphill.healthcare_booking_system.repository.DoctorRepository;
import com.uphill.healthcare_booking_system.repository.entity.Doctor;

@Service
public class DoctorService {
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private AppointmentRepository appointmentRepository;

    // For simplicity reasons, this will return a Doctor instead of a DoctorDomain, but it should return a domain on the service layer
    public Doctor findAndLockAvailableDoctor(String specialty, LocalDateTime start, LocalDateTime end) {
        Doctor candidate = doctorRepository
                .findFirstAvailableBySpecialtyAndWindow(specialty, start, end, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElseThrow(() -> new NoAvailableDoctorException(specialty, start, end));

        Doctor locked = doctorRepository.lockById(candidate.getId());

        boolean stillFree = appointmentRepository.existsOverlapForDoctor(locked.getId(), start, end);
        if (stillFree) {
            candidate = doctorRepository
                    .findFirstAvailableBySpecialtyAndWindow(specialty, start, end, PageRequest.of(0, 1))
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new NoAvailableDoctorException(specialty, start, end));

            locked = doctorRepository.lockById(candidate.getId());
            if (appointmentRepository.existsOverlapForDoctor(locked.getId(), start, end)) {
                throw new NoAvailableDoctorException(specialty, start, end);
            }
        }
        return locked;
    }
}
