package com.uphill.healthcare_booking_system.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(DoctorService.class);

    // For simplicity reasons, this will return a Doctor instead of a DoctorDomain, but it should return a domain on the service layer
    public Doctor findAndLockAvailableDoctor(String specialty, LocalDateTime start, LocalDateTime end) {
        Doctor candidate = doctorRepository
                .findFirstAvailableBySpecialtyAndWindow(specialty, start, end, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElseThrow(() -> new NoAvailableDoctorException(specialty, start, end));

        Doctor locked = doctorRepository.lockById(candidate.getId());
        log.info("Doctor locked: id={}, name={}", locked.getId(), locked.getName());

        // This was an improvement suggested by ChatGPT in order to confirm that the doctor is still available for the appointment
        boolean stillFree = appointmentRepository.existsOverlapForDoctor(locked.getId(), start, end);
        if (stillFree) {
            candidate = doctorRepository
                    .findFirstAvailableBySpecialtyAndWindow(specialty, start, end, PageRequest.of(0, 1))
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new NoAvailableDoctorException(specialty, start, end));

            locked = doctorRepository.lockById(candidate.getId());
            log.info("Doctor locked: id={}, name={}", locked.getId(), locked.getName());
            if (appointmentRepository.existsOverlapForDoctor(locked.getId(), start, end)) {
                log.error("Doctor is still free for appointment id={}", locked.getId());
                throw new NoAvailableDoctorException(specialty, start, end);
            }
        }
        return locked;
    }
}
