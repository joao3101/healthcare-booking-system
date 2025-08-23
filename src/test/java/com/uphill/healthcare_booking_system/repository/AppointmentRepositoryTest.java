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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AppointmentRepositoryTest {

    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private PatientRepository patientRepository;

    private Doctor doctor;
    private Room room;
    private Patient patient;

    private Appointment a1;
    private Appointment a2;

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
        patient.setEmail("test_mail@mail.com");
        patient = patientRepository.save(patient);

        // 10:00–11:00
        a1 = new Appointment();
        a1.setDoctor(doctor);
        a1.setRoom(room);
        a1.setPatient(patient);
        a1.setStartTime(LocalDateTime.of(2025, 8, 23, 10, 0));
        a1.setEndTime(LocalDateTime.of(2025, 8, 23, 11, 0));

        // 11:30–12:30
        a2 = new Appointment();
        a2.setDoctor(doctor);
        a2.setRoom(room);
        a2.setPatient(patient);
        a2.setStartTime(LocalDateTime.of(2025, 8, 23, 11, 30));
        a2.setEndTime(LocalDateTime.of(2025, 8, 23, 12, 30));

        appointmentRepository.saveAll(List.of(a1, a2));
    }

    @Test
    void testSaveAndFindById() {
        Appointment appt = new Appointment();
        appt.setDoctor(doctor);
        appt.setRoom(room);
        appt.setPatient(patient);
        appt.setStartTime(LocalDateTime.of(2025, 8, 23, 13, 0));
        appt.setEndTime(LocalDateTime.of(2025, 8, 23, 14, 0));

        Appointment saved = appointmentRepository.save(appt);

        Optional<Appointment> found = appointmentRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getDoctor().getId()).isEqualTo(doctor.getId());
        assertThat(found.get().getRoom().getId()).isEqualTo(room.getId());
        assertThat(found.get().getPatient().getId()).isEqualTo(patient.getId());
        assertThat(found.get().getStartTime()).isEqualTo(LocalDateTime.of(2025, 8, 23, 13, 0));
    }

    @Test
    void testFindAll() {
        List<Appointment> all = appointmentRepository.findAll();
        assertThat(all).hasSize(2);
        assertThat(all).extracting(Appointment::getStartTime)
                .containsExactlyInAnyOrder(
                        LocalDateTime.of(2025, 8, 23, 10, 0),
                        LocalDateTime.of(2025, 8, 23, 11, 30)
                );
    }

    @Test
    void testFindByDoctorOverlappingTimes() {
        // Query window 10:30–10:45 overlaps only a1 (10:00–11:00)
        var results = appointmentRepository
                .findByDoctorAndStartTimeLessThanAndEndTimeGreaterThan(
                        doctor,
                        LocalDateTime.of(2025, 8, 23, 10, 45), // end
                        LocalDateTime.of(2025, 8, 23, 10, 30)  // start
                );

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStartTime()).isEqualTo(LocalDateTime.of(2025, 8, 23, 10, 0));
    }

    @Test
    void testFindByRoomOverlappingTimes() {
        // Query window 11:45–12:00 overlaps only a2 (11:30–12:30)
        var results = appointmentRepository
                .findByRoomAndStartTimeLessThanAndEndTimeGreaterThan(
                        room,
                        LocalDateTime.of(2025, 8, 23, 12, 0),  // end
                        LocalDateTime.of(2025, 8, 23, 11, 45)  // start
                );

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStartTime()).isEqualTo(LocalDateTime.of(2025, 8, 23, 11, 30));
    }

    @Test
    void testOverlapBoundaryNoHit() {
        // Query window exactly ends at 10:00 — with < and > semantics, this should NOT overlap a1.
        var results = appointmentRepository
                .findByDoctorAndStartTimeLessThanAndEndTimeGreaterThan(
                        doctor,
                        LocalDateTime.of(2025, 8, 23, 10, 0), // end
                        LocalDateTime.of(2025, 8, 23, 9, 0)   // start
                );
        assertThat(results).isEmpty();
    }
}
