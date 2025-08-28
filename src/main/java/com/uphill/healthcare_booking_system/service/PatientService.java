package com.uphill.healthcare_booking_system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.uphill.healthcare_booking_system.repository.PatientRepository;
import com.uphill.healthcare_booking_system.repository.entity.Patient;

@Service
public class PatientService {
    @Autowired
    private PatientRepository patientRepository;
    private static final Logger log = LoggerFactory.getLogger(PatientService.class);

    // For simplicity reasons, this will return a Patient instead of a PatientDomain, but it should return a domain on the service layer
    public Patient findOrCreatePatient(String email, String name) {
        Patient patient = patientRepository.findByEmail(email);
        if (patient == null) {
            try {
                patient = new Patient();
                patient.setName(name);
                patient.setEmail(email);
                patientRepository.save(patient);
                log.info("Patient created: id={}, email={}, name={}", patient.getId(), patient.getEmail(), patient.getName());
            } catch (DataIntegrityViolationException e) {
                log.info("Patient already exists: email={}", email);
                patient = patientRepository.findByEmail(email);
            }
        }
        return patient;
    }
}
