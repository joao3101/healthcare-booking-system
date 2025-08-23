package com.uphill.healthcare_booking_system.repository;

import com.uphill.healthcare_booking_system.repository.entity.Appointment;
import com.uphill.healthcare_booking_system.repository.entity.Doctor;
import com.uphill.healthcare_booking_system.repository.entity.Patient;
import com.uphill.healthcare_booking_system.repository.entity.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AppointmentOverlapTest {

    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private PatientRepository patientRepository;

    private Doctor doctor;
    private Room room;
    private Patient patient;

    private Appointment a1, a2, a3;

    @BeforeEach
    void setUp() {
        doctor = new Doctor();
        doctor.setName("Dr. Strange");
        doctor.setSpecialty("Magic");
        doctor = doctorRepository.save(doctor);

        room = new Room();
        room.setName("Room A");
        room = roomRepository.save(room);

        patient = new Patient();
        patient.setName("Tony Stark");
        patient.setEmail("tony.stark@mail.com");
        patient = patientRepository.save(patient);

        // a1: 10:00–11:00
        a1 = new Appointment();
        a1.setDoctor(doctor);
        a1.setRoom(room);
        a1.setPatient(patient);
        a1.setStartTime(LocalDateTime.of(2025, 8, 23, 10, 0));
        a1.setEndTime(LocalDateTime.of(2025, 8, 23, 11, 0));

        // a2: 10:30–11:30 (partial overlap with a1)
        a2 = new Appointment();
        a2.setDoctor(doctor);
        a2.setRoom(room);
        a2.setPatient(patient);
        a2.setStartTime(LocalDateTime.of(2025, 8, 23, 10, 30));
        a2.setEndTime(LocalDateTime.of(2025, 8, 23, 11, 30));

        // a3: 12:00–13:00 (no overlap)
        a3 = new Appointment();
        a3.setDoctor(doctor);
        a3.setRoom(room);
        a3.setPatient(patient);
        a3.setStartTime(LocalDateTime.of(2025, 8, 23, 12, 0));
        a3.setEndTime(LocalDateTime.of(2025, 8, 23, 13, 0));

        appointmentRepository.saveAll(List.of(a1, a2, a3));
    }

    @Test
    void testDoctorOverlap() {
        // Query: 10:15–10:45 → should return a1 and a2
        LocalDateTime queryStart = LocalDateTime.of(2025, 8, 23, 10, 15);
        LocalDateTime queryEnd = LocalDateTime.of(2025, 8, 23, 10, 45);

        List<Appointment> results = appointmentRepository
                .findByDoctorAndStartTimeLessThanAndEndTimeGreaterThan(doctor, queryEnd, queryStart);

        assertThat(results).hasSize(2).containsExactlyInAnyOrder(a1, a2);
    }

    @Test
    void testRoomOverlap() {
        // Query: 11:15–12:15 → should return a2 and a3
        LocalDateTime queryStart = LocalDateTime.of(2025, 8, 23, 11, 15);
        LocalDateTime queryEnd = LocalDateTime.of(2025, 8, 23, 12, 15);

        List<Appointment> results = appointmentRepository
                .findByRoomAndStartTimeLessThanAndEndTimeGreaterThan(room, queryEnd, queryStart);

        assertThat(results).hasSize(2).containsExactlyInAnyOrder(a2, a3);
    }

    @Test
    void testNoOverlap() {
        // Query: 9:00–9:30 → should return empty
        LocalDateTime queryStart = LocalDateTime.of(2025, 8, 23, 9, 0);
        LocalDateTime queryEnd = LocalDateTime.of(2025, 8, 23, 9, 30);

        List<Appointment> results = appointmentRepository
                .findByDoctorAndStartTimeLessThanAndEndTimeGreaterThan(doctor, queryEnd, queryStart);

        assertThat(results).isEmpty();
    }
}
