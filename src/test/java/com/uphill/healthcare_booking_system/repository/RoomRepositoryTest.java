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
class RoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Test
    @DisplayName("Should find available room when no conflicting appointments exist")
    void findAvailableRoom_success() {
        // given
        Room room = new Room();
        room.setName("Room A");
        room.setLocation("Floor 1");
        roomRepository.save(room);

        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(2);

        // when
        List<Room> result = roomRepository.findFirstAvailableByWindow(
                start, end, PageRequest.of(0, 1));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Room A");
    }

    @Test
    @DisplayName("Should NOT find room if overlapping appointment exists")
    void findAvailableRoom_conflict() {
        // given
        Room room = new Room();
        room.setName("Room B");
        room.setLocation("Floor 2");
        roomRepository.save(room);

        Doctor doctor = new Doctor();
        doctor.setName("Dr. Strange");
        doctor.setSpecialty("Cardiology");
        doctorRepository.save(doctor);

        Patient patient = new Patient();
        patient.setName("Jane Doe");
        patient.setEmail("jane.doe@example.com");
        patientRepository.save(patient);

        Appointment appt = new Appointment();
        appt.setDoctor(doctor);
        appt.setPatient(patient);
        appt.setRoom(room);
        appt.setStartTime(LocalDateTime.now().plusHours(1));
        appt.setEndTime(LocalDateTime.now().plusHours(3));
        appt.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepository.save(appt);

        LocalDateTime start = LocalDateTime.now().plusHours(2);
        LocalDateTime end = LocalDateTime.now().plusHours(4);

        // when
        List<Room> result = roomRepository.findFirstAvailableByWindow(
                start, end, PageRequest.of(0, 1));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should lock room by id")
    void lockById_success() {
        // given
        Room room = new Room();
        room.setName("Room C");
        room.setLocation("Floor 3");
        roomRepository.save(room);

        // when
        Room locked = roomRepository.lockById(room.getId());

        // then
        assertThat(locked).isNotNull();
        assertThat(locked.getId()).isEqualTo(room.getId());
    }
}
