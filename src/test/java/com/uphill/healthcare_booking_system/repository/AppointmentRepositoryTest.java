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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AppointmentRepositoryTest {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Test
    @DisplayName("Should detect overlap for doctor when appointment exists")
    void existsOverlapForDoctor_true() {
        // given
        Doctor doctor = new Doctor();
        doctor.setName("Dr. House");
        doctor.setSpecialty("Diagnostics");
        doctorRepository.save(doctor);

        Room room = new Room();
        room.setName("Room A");
        room.setLocation("Building 1");
        roomRepository.save(room);

        Patient patient = new Patient();
        patient.setName("John Doe");
        patient.setEmail("john@example.com");
        patientRepository.save(patient);

        Appointment appt = new Appointment();
        appt.setDoctor(doctor);
        appt.setRoom(room);
        appt.setPatient(patient);
        appt.setStartTime(LocalDateTime.now().plusHours(1));
        appt.setEndTime(LocalDateTime.now().plusHours(3));
        appt.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepository.save(appt);

        // when
        boolean exists = appointmentRepository.existsOverlapForDoctor(
                doctor.getId(),
                LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusHours(4));

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should NOT detect overlap for doctor when no conflicting appointments")
    void existsOverlapForDoctor_false() {
        // given
        Doctor doctor = new Doctor();
        doctor.setName("Dr. Strange");
        doctor.setSpecialty("Cardiology");
        doctorRepository.save(doctor);

        Room room = new Room();
        room.setName("Room B");
        room.setLocation("Building 2");
        roomRepository.save(room);

        Patient patient = new Patient();
        patient.setName("Jane Doe");
        patient.setEmail("jane@example.com");
        patientRepository.save(patient);

        Appointment appt = new Appointment();
        appt.setDoctor(doctor);
        appt.setRoom(room);
        appt.setPatient(patient);
        appt.setStartTime(LocalDateTime.now().plusHours(1));
        appt.setEndTime(LocalDateTime.now().plusHours(2));
        appt.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepository.save(appt);

        // when
        boolean exists = appointmentRepository.existsOverlapForDoctor(
                doctor.getId(),
                LocalDateTime.now().plusHours(3),
                LocalDateTime.now().plusHours(4));

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should detect overlap for room when appointment exists")
    void existsOverlapForRoom_true() {
        // given
        Doctor doctor = new Doctor();
        doctor.setName("Dr. Who");
        doctor.setSpecialty("Neurology");
        doctorRepository.save(doctor);

        Room room = new Room();
        room.setName("Room C");
        room.setLocation("Building 3");
        roomRepository.save(room);

        Patient patient = new Patient();
        patient.setName("Alice Doe");
        patient.setEmail("alice@example.com");
        patientRepository.save(patient);

        Appointment appt = new Appointment();
        appt.setDoctor(doctor);
        appt.setRoom(room);
        appt.setPatient(patient);
        appt.setStartTime(LocalDateTime.now().plusHours(2));
        appt.setEndTime(LocalDateTime.now().plusHours(5));
        appt.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepository.save(appt);

        // when
        boolean exists = appointmentRepository.existsOverlapForRoom(
                room.getId(),
                LocalDateTime.now().plusHours(3),
                LocalDateTime.now().plusHours(4));

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should NOT detect overlap for room when no conflicting appointments")
    void existsOverlapForRoom_false() {
        // given
        Doctor doctor = new Doctor();
        doctor.setName("Dr. Watson");
        doctor.setSpecialty("General Medicine");
        doctorRepository.save(doctor);

        Room room = new Room();
        room.setName("Room D");
        room.setLocation("Building 4");
        roomRepository.save(room);

        Patient patient = new Patient();
        patient.setName("Bob Doe");
        patient.setEmail("bob@example.com");
        patientRepository.save(patient);

        Appointment appt = new Appointment();
        appt.setDoctor(doctor);
        appt.setRoom(room);
        appt.setPatient(patient);
        appt.setStartTime(LocalDateTime.now().plusHours(5));
        appt.setEndTime(LocalDateTime.now().plusHours(6));
        appt.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepository.save(appt);

        // when
        boolean exists = appointmentRepository.existsOverlapForRoom(
                room.getId(),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2));

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("findAll with pagination should return appointments")
    void findAll_paginated() {
        // given
        Doctor doc = new Doctor();
        doc.setName("Dr. Strange");
        doc.setSpecialty("Cardiology");
        doctorRepository.save(doc);

        Room room = new Room();
        room.setName("Room A");
        room.setLocation("First Floor");
        roomRepository.save(room);

        Patient patient = new Patient();
        patient.setName("John Doe");
        patient.setEmail("john.doe@example.com");
        patientRepository.save(patient);

        Appointment appt = new Appointment();
        appt.setDoctor(doc);
        appt.setRoom(room);
        appt.setPatient(patient);
        appt.setStartTime(LocalDateTime.now().plusHours(1));
        appt.setEndTime(LocalDateTime.now().plusHours(2));
        appt.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepository.save(appt);

        // when
        Page<Appointment> page = appointmentRepository.findAll(PageRequest.of(0, 10));

        // then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getDoctor().getName()).isEqualTo("Dr. Strange");
    }
}
