package com.uphill.healthcare_booking_system.repository;

import com.uphill.healthcare_booking_system.repository.entity.Patient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PatientRepositoryTest {

    @Autowired
    private PatientRepository patientRepository;

    @Test
    @DisplayName("findByEmail should return patient when email exists")
    void findByEmail_success() {
        // given
        Patient patient = new Patient();
        patient.setName("Alice");
        patient.setEmail("alice@example.com");
        patientRepository.save(patient);

        // when
        Patient found = patientRepository.findByEmail("alice@example.com");

        // then
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Alice");
        assertThat(found.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("findByEmail should return null when email does not exist")
    void findByEmail_notFound() {
        // when
        Patient found = patientRepository.findByEmail("ghost@example.com");

        // then
        assertThat(found).isNull();
    }

    @Test
    @DisplayName("save and findById should persist and retrieve patient")
    void saveAndFindById() {
        // given
        Patient patient = new Patient();
        patient.setName("Bob");
        patient.setEmail("bob@example.com");
        Patient saved = patientRepository.save(patient);

        // when
        Optional<Patient> found = patientRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("bob@example.com");
    }
}
