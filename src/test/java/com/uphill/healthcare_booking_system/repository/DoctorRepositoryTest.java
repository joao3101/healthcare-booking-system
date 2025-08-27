package com.uphill.healthcare_booking_system.repository;

import com.uphill.healthcare_booking_system.enums.AppointmentStatus;
import com.uphill.healthcare_booking_system.repository.entity.Appointment;
import com.uphill.healthcare_booking_system.repository.entity.Doctor;
import com.uphill.healthcare_booking_system.repository.entity.Patient;
import com.uphill.healthcare_booking_system.repository.entity.Room;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DoctorRepositoryTest {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Test
    @DisplayName("Should find available doctor when no conflicting appointments exist")
    void findAvailableDoctor_success() {
        // given
        Doctor doc = new Doctor();
        doc.setName("Dr. Strange");
        doc.setSpecialty("Cardiology");
        doctorRepository.save(doc);

        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(2);

        // when
        List<Doctor> result = doctorRepository.findFirstAvailableBySpecialtyAndWindow(
                "Cardiology", start, end, PageRequest.of(0, 1));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Dr. Strange");
    }

    @Test
    @DisplayName("Should NOT find doctor if overlapping appointment exists")
    void findAvailableDoctor_conflict() {
        // given
        Doctor doc = new Doctor();
        doc.setName("Dr. House");
        doc.setSpecialty("Cardiology");
        doctorRepository.save(doc);

        Patient patient = new Patient();
        patient.setName("John Doe");
        patient.setEmail("john.doe@example.com");
        patientRepository.save(patient);

        Room room = new Room();
        room.setName("Room 1");
        room.setLocation("Building 1");
        roomRepository.save(room);

        Appointment appt = new Appointment();
        appt.setDoctor(doc);
        appt.setPatient(patient);
        appt.setRoom(room);
        appt.setStartTime(LocalDateTime.now().plusHours(1));
        appt.setEndTime(LocalDateTime.now().plusHours(3));
        appt.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepository.save(appt);

        LocalDateTime start = LocalDateTime.now().plusHours(2);
        LocalDateTime end = LocalDateTime.now().plusHours(4);

        // when
        List<Doctor> result = doctorRepository.findFirstAvailableBySpecialtyAndWindow(
                "Cardiology", start, end, PageRequest.of(0, 1));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should lock doctor by id")
    void lockById_success() {
        // given
        Doctor doc = new Doctor();
        doc.setName("Dr. Who");
        doc.setSpecialty("Neurology");
        doctorRepository.save(doc);

        // when
        Doctor locked = doctorRepository.lockById(doc.getId());

        // then
        assertThat(locked).isNotNull();
        assertThat(locked.getId()).isEqualTo(doc.getId());
    }
}
