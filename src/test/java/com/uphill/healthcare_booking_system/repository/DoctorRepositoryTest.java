package com.uphill.healthcare_booking_system.repository;

import com.uphill.healthcare_booking_system.repository.entity.Doctor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DoctorRepositoryTest {

    @Autowired
    private DoctorRepository doctorRepository;

    @Test
    void testSaveAndFindById() {
        Doctor doctor = new Doctor();
        doctor.setName("Dr. House");
        doctor.setSpecialty("Diagnostics");

        Doctor saved = doctorRepository.save(doctor);

        Optional<Doctor> found = doctorRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Dr. House");
        assertThat(found.get().getSpecialty()).isEqualTo("Diagnostics");
    }

    @Test
    void testFindBySpecialty() {
        Doctor cardiologist = new Doctor();
        cardiologist.setName("Dr. Heart");
        cardiologist.setSpecialty("Cardiology");

        Doctor neurologist = new Doctor();
        neurologist.setName("Dr. Brain");
        neurologist.setSpecialty("Neurology");

        doctorRepository.saveAll(List.of(cardiologist, neurologist));

        List<Doctor> cardiologists = doctorRepository.findBySpecialty("Cardiology");

        assertThat(cardiologists).hasSize(1);
        assertThat(cardiologists.get(0).getName()).isEqualTo("Dr. Heart");
    }

    @Test
    void testFindAll() {
        Doctor d1 = new Doctor();
        d1.setName("Dr. One");
        d1.setSpecialty("General");

        Doctor d2 = new Doctor();
        d2.setName("Dr. Two");
        d2.setSpecialty("Surgery");

        doctorRepository.saveAll(List.of(d1, d2));

        List<Doctor> doctors = doctorRepository.findAll();

        assertThat(doctors).hasSize(2).extracting(Doctor::getName)
                .containsExactlyInAnyOrder("Dr. One", "Dr. Two");
    }
}
