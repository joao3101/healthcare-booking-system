package com.uphill.healthcare_booking_system.service;

import com.uphill.healthcare_booking_system.domain.AppointmentDomain;
import com.uphill.healthcare_booking_system.domain.PatientDomain;
import com.uphill.healthcare_booking_system.domain.exceptions.InvalidAppointmentWindowException;
import com.uphill.healthcare_booking_system.domain.exceptions.NoAvailableDoctorException;
import com.uphill.healthcare_booking_system.domain.exceptions.NoAvailableRoomException;
import com.uphill.healthcare_booking_system.repository.AppointmentRepository;
import com.uphill.healthcare_booking_system.repository.DoctorRepository;
import com.uphill.healthcare_booking_system.repository.PatientRepository;
import com.uphill.healthcare_booking_system.repository.RoomRepository;
import com.uphill.healthcare_booking_system.repository.entity.Appointment;
import com.uphill.healthcare_booking_system.repository.entity.Doctor;
import com.uphill.healthcare_booking_system.repository.entity.Patient;
import com.uphill.healthcare_booking_system.repository.entity.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

        @Mock
        private DoctorRepository doctorRepository;
        @Mock
        private RoomRepository roomRepository;
        @Mock
        private AppointmentRepository appointmentRepository;
        @Mock
        private PatientRepository patientRepository;

        @InjectMocks
        private AppointmentService appointmentService;

        private AppointmentDomain baseRequest;

        @BeforeEach
        void setUp() {
                PatientDomain patient = new PatientDomain();
                patient.setId(1L);
                patient.setName("John Doe");
                patient.setEmail("test_mail@mail.com");

                baseRequest = new AppointmentDomain();
                baseRequest.setSpecialty("Cardiology");
                baseRequest.setPatient(patient);
                baseRequest.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
                baseRequest.setEndTime(LocalDateTime.of(2025, 1, 1, 11, 0));
        }

        @Test
        void shouldBookAppointmentWhenDoctorAndRoomAvailable() {
                Doctor doctor = new Doctor();
                doctor.setId(100L);

                Room room = new Room();
                room.setId(200L);

                Patient patient = new Patient();
                patient.setId(1L);
                patient.setName("John Doe");
                patient.setEmail("test_mail@mail.com");

                when(patientRepository.findByEmail("test_mail@mail.com")).thenReturn(patient);

                when(doctorRepository.findFirstAvailableBySpecialtyAndWindow(
                                any(), any(), any(), any(Pageable.class)))
                                .thenReturn(List.of(doctor));
                when(doctorRepository.lockById(100L)).thenReturn(doctor);
                when(appointmentRepository.existsOverlapForDoctor(eq(100L), any(), any())).thenReturn(false);

                when(roomRepository.findFirstAvailableByWindow(any(), any(), any(Pageable.class)))
                                .thenReturn(List.of(room));
                when(roomRepository.lockById(200L)).thenReturn(room);
                when(appointmentRepository.existsOverlapForRoom(eq(200L), any(), any())).thenReturn(false);

                when(appointmentRepository.save(any(Appointment.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0)); // return the same object

                AppointmentDomain appt = appointmentService.bookAppointment(baseRequest);

                assertNotNull(appt);
                assertEquals(100L, appt.getDoctor().getId());
                assertEquals(200L, appt.getRoom().getId());
                verify(appointmentRepository).save(any(Appointment.class));
        }

        @Test
        void shouldThrowWhenNoDoctorAvailable() {
                Patient patient = new Patient();
                patient.setId(1L);
                patient.setName("John Doe");
                patient.setEmail("test_mail@mail.com");

                when(patientRepository.findByEmail("test_mail@mail.com")).thenReturn(patient);

                when(doctorRepository.findFirstAvailableBySpecialtyAndWindow(any(), any(), any(), any(Pageable.class)))
                                .thenReturn(List.of());

                assertThrows(NoAvailableDoctorException.class,
                                () -> appointmentService.bookAppointment(baseRequest));
        }

        @Test
        void shouldThrowWhenDoctorIsTakenAfterLock() {
                Doctor doctor = new Doctor();
                doctor.setId(101L);

                Patient patient = new Patient();
                patient.setId(1L);
                patient.setName("John Doe");
                patient.setEmail("test_mail@mail.com");

                when(patientRepository.findByEmail("test_mail@mail.com")).thenReturn(patient);

                when(doctorRepository.findFirstAvailableBySpecialtyAndWindow(any(), any(), any(), any(Pageable.class)))
                                .thenReturn(List.of(doctor));
                when(doctorRepository.lockById(101L)).thenReturn(doctor);

                // Simulate overlap found
                when(appointmentRepository.existsOverlapForDoctor(eq(101L), any(), any())).thenReturn(true);

                assertThrows(NoAvailableDoctorException.class,
                                () -> appointmentService.bookAppointment(baseRequest));
        }

        @Test
        void shouldThrowWhenNoRoomAvailable() {
                Doctor doctor = new Doctor();
                doctor.setId(100L);

                Patient patient = new Patient();
                patient.setId(1L);
                patient.setName("John Doe");
                patient.setEmail("test_mail@mail.com");

                when(patientRepository.findByEmail("test_mail@mail.com")).thenReturn(patient);

                when(doctorRepository.findFirstAvailableBySpecialtyAndWindow(any(), any(), any(), any(Pageable.class)))
                                .thenReturn(List.of(doctor));
                when(doctorRepository.lockById(100L)).thenReturn(doctor);
                when(appointmentRepository.existsOverlapForDoctor(eq(100L), any(), any())).thenReturn(false);

                when(roomRepository.findFirstAvailableByWindow(any(), any(), any(Pageable.class)))
                                .thenReturn(List.of());

                assertThrows(NoAvailableRoomException.class,
                                () -> appointmentService.bookAppointment(baseRequest));
        }

        @Test
        void shouldThrowWhenRoomIsTakenAfterLock() {
                Doctor doctor = new Doctor();
                doctor.setId(100L);

                Room room = new Room();
                room.setId(200L);

                Patient patient = new Patient();
                patient.setId(1L);
                patient.setName("John Doe");
                patient.setEmail("test_mail@mail.com");

                when(patientRepository.findByEmail("test_mail@mail.com")).thenReturn(patient);

                when(doctorRepository.findFirstAvailableBySpecialtyAndWindow(any(), any(), any(), any(Pageable.class)))
                                .thenReturn(List.of(doctor));
                when(doctorRepository.lockById(100L)).thenReturn(doctor);
                when(appointmentRepository.existsOverlapForDoctor(eq(100L), any(), any())).thenReturn(false);

                when(roomRepository.findFirstAvailableByWindow(any(), any(), any(Pageable.class)))
                                .thenReturn(List.of(room));
                when(roomRepository.lockById(200L)).thenReturn(room);
                when(appointmentRepository.existsOverlapForRoom(eq(200L), any(), any())).thenReturn(true);

                assertThrows(NoAvailableRoomException.class,
                                () -> appointmentService.bookAppointment(baseRequest));
        }

        @Test
        void shouldThrowForInvalidTimeWindow() {
                baseRequest.setEndTime(baseRequest.getStartTime().minusMinutes(30));

                assertThrows(InvalidAppointmentWindowException.class,
                                () -> appointmentService.bookAppointment(baseRequest));
        }

}
