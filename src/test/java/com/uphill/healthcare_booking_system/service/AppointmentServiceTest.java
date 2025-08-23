package com.uphill.healthcare_booking_system.service;

import com.uphill.healthcare_booking_system.repository.AppointmentRepository;
import com.uphill.healthcare_booking_system.repository.entity.Appointment;
import com.uphill.healthcare_booking_system.repository.entity.Doctor;
import com.uphill.healthcare_booking_system.repository.entity.Patient;
import com.uphill.healthcare_booking_system.repository.entity.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private Doctor doctor;
    private Room room;
    private Patient patient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setName("Dr. Strange");
        doctor.setSpecialty("Magic");

        room = new Room();
        room.setId(1L);
        room.setName("Room A");

        patient = new Patient();
        patient.setId(1L);
        patient.setName("Tony Stark");
    }

    @Test
    void testBookNonOverlappingAppointment() {
        Appointment appt = new Appointment();
        appt.setDoctor(doctor);
        appt.setRoom(room);
        appt.setPatient(patient);
        appt.setStartTime(LocalDateTime.of(2025, 8, 23, 10, 0));
        appt.setEndTime(LocalDateTime.of(2025, 8, 23, 11, 0));

        when(appointmentRepository.findByDoctorAndStartTimeLessThanAndEndTimeGreaterThan(
                doctor, appt.getEndTime(), appt.getStartTime()))
                .thenReturn(Collections.emptyList());

        when(appointmentRepository.findByRoomAndStartTimeLessThanAndEndTimeGreaterThan(
                room, appt.getEndTime(), appt.getStartTime()))
                .thenReturn(Collections.emptyList());

        when(appointmentRepository.save(appt)).thenReturn(appt);

        Appointment saved = appointmentService.bookAppointment(appt);

        assertThat(saved).isNotNull();
        verify(appointmentRepository, times(1)).save(appt);
    }

    @Test
    void testCannotBookOverlappingDoctor() {
        Appointment conflict = new Appointment();
        conflict.setDoctor(doctor);
        conflict.setRoom(room);
        conflict.setPatient(patient);
        conflict.setStartTime(LocalDateTime.of(2025, 8, 23, 10, 30));
        conflict.setEndTime(LocalDateTime.of(2025, 8, 23, 11, 30));

        when(appointmentRepository.findByDoctorAndStartTimeLessThanAndEndTimeGreaterThan(
                doctor, conflict.getEndTime(), conflict.getStartTime()))
                .thenReturn(List.of(new Appointment()));

        assertThatThrownBy(() -> appointmentService.bookAppointment(conflict))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Doctor is not available");

        verify(appointmentRepository, never()).save(conflict);
    }

    @Test
    void testCannotBookOverlappingRoom() {
        Appointment conflict = new Appointment();
        conflict.setDoctor(doctor);
        conflict.setRoom(room);
        conflict.setPatient(patient);
        conflict.setStartTime(LocalDateTime.of(2025, 8, 23, 10, 30));
        conflict.setEndTime(LocalDateTime.of(2025, 8, 23, 11, 30));

        when(appointmentRepository.findByDoctorAndStartTimeLessThanAndEndTimeGreaterThan(
                doctor, conflict.getEndTime(), conflict.getStartTime()))
                .thenReturn(Collections.emptyList());

        when(appointmentRepository.findByRoomAndStartTimeLessThanAndEndTimeGreaterThan(
                room, conflict.getEndTime(), conflict.getStartTime()))
                .thenReturn(List.of(new Appointment()));

        assertThatThrownBy(() -> appointmentService.bookAppointment(conflict))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Room is not available");

        verify(appointmentRepository, never()).save(conflict);
    }
}
