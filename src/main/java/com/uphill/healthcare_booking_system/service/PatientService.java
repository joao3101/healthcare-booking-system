package com.uphill.healthcare_booking_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.uphill.healthcare_booking_system.repository.PatientRepository;
import com.uphill.healthcare_booking_system.repository.entity.Patient;

@Service
public class PatientService {
    @Autowired
    private PatientRepository patientRepository;

    public Patient findOrCreatePatient(String email, String name) {
        Patient patient = patientRepository.findByEmail(email);
        if (patient == null) {
            try {
                patient = new Patient();
                patient.setName(name);
                patient.setEmail(email);
                patientRepository.save(patient);
            } catch (DataIntegrityViolationException e) {
                patient = patientRepository.findByEmail(email);
            }
        }
        return patient;
    }
}
