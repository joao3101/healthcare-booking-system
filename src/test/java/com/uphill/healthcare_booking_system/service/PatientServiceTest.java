package com.uphill.healthcare_booking_system.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.uphill.healthcare_booking_system.repository.PatientRepository;
import com.uphill.healthcare_booking_system.repository.entity.Patient;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @InjectMocks
    private PatientService patientService;

    @Mock
    private PatientRepository patientRepository;

    private final String email = "john.doe@example.com";
    private final String name = "John Doe";

    @Test
    void findOrCreatePatient_existingPatient_returnsPatient() {
        Patient existing = new Patient();
        existing.setId(1L);
        existing.setEmail(email);
        existing.setName(name);

        when(patientRepository.findByEmail(email)).thenReturn(existing);

        Patient result = patientService.findOrCreatePatient(email, name);

        assertThat(result).isEqualTo(existing);
        verify(patientRepository, never()).save(any());
    }

    @Test
    void findOrCreatePatient_newPatient_savedSuccessfully() {
        when(patientRepository.findByEmail(email)).thenReturn(null);

        Patient saved = new Patient();
        saved.setId(2L);
        saved.setEmail(email);
        saved.setName(name);

        when(patientRepository.save(any())).thenReturn(saved);

        Patient result = patientService.findOrCreatePatient(email, name);

        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getName()).isEqualTo(name);
        verify(patientRepository).save(any());
    }

    @Test
    void findOrCreatePatient_saveFails_dueToRace_findAgain() {
        when(patientRepository.findByEmail(email))
                .thenReturn(null) // first call, not found
                .thenReturn(new Patient() {{ setId(3L); setEmail(email); setName(name); }}); // second call

        doThrow(DataIntegrityViolationException.class).when(patientRepository).save(any());

        Patient result = patientService.findOrCreatePatient(email, name);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        verify(patientRepository).save(any());
        verify(patientRepository, times(2)).findByEmail(email);
    }
}

