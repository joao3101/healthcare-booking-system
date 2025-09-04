package com.uphill.healthcare_booking_system.service;

import com.uphill.healthcare_booking_system.domain.AppointmentDomain;
import com.uphill.healthcare_booking_system.domain.PatientDomain;
import com.uphill.healthcare_booking_system.domain.exceptions.InvalidAppointmentWindowException;
import com.uphill.healthcare_booking_system.integration.DoctorCalendarClient;
import com.uphill.healthcare_booking_system.integration.EmailClient;
import com.uphill.healthcare_booking_system.integration.RoomReservationClient;
import com.uphill.healthcare_booking_system.repository.AppointmentRepository;
import com.uphill.healthcare_booking_system.repository.entity.Appointment;
import com.uphill.healthcare_booking_system.repository.entity.Doctor;
import com.uphill.healthcare_booking_system.repository.entity.Patient;
import com.uphill.healthcare_booking_system.repository.entity.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

        @InjectMocks
        private AppointmentService appointmentService;

        @Mock
        private PatientService patientService;

        @Mock
        private DoctorService doctorService;

        @Mock
        private RoomService roomService;

        @Mock
        private AppointmentRepository appointmentRepository;

        @Mock
        private DoctorCalendarClient doctorCalendarClient;

        @Mock
        private RoomReservationClient roomReservationClient;

        @Mock
        private EmailClient emailClient;

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
                baseRequest.setStartTime(LocalDateTime.now().plusHours(1));
                baseRequest.setEndTime(LocalDateTime.now().plusHours(2));
        }

        @Test
        void bookAppointment_success() {
                // given
                LocalDateTime start = LocalDateTime.now().plusHours(1);
                LocalDateTime end = start.plusHours(1);
                AppointmentDomain input = baseRequest;

                Patient patient = new Patient();
                patient.setId(1L);
                Doctor doctor = new Doctor();
                doctor.setId(2L);
                Room room = new Room();
                room.setId(3L);

                Appointment saved = new Appointment();
                saved.setId(100L);
                saved.setDoctor(doctor);
                saved.setRoom(room);
                saved.setPatient(patient);
                saved.setStartTime(start);
                saved.setEndTime(end);

                when(patientService.findOrCreatePatient(any(), any())).thenReturn(patient);
                when(doctorService.findAndLockAvailableDoctor(any(), any(), any())).thenReturn(doctor);
                when(roomService.findAndLockAvailableRoom(any(), any())).thenReturn(room);
                when(appointmentRepository.save(any())).thenReturn(saved);

                // when
                AppointmentDomain result = appointmentService.bookAppointment(input);

                // then
                assertThat(result.getId()).isEqualTo(100L);
                assertThat(result.getDoctor().getId()).isEqualTo(2L);
                assertThat(result.getRoom().getId()).isEqualTo(3L);

                // verify external interactions
                verify(doctorCalendarClient).reserveSlot(eq(doctor.getId()), eq(start), eq(end));
                verify(roomReservationClient).reserveRoom(eq(room.getId()), eq(start), eq(end));
                verify(emailClient).sendAppointmentConfirmation(eq(patient.getEmail()), any(), any());
        }

        @Test
        void bookAppointment_invalidWindow_throwsException() {
                AppointmentDomain input = baseRequest;
                input.setStartTime(LocalDateTime.now().plusHours(2));
                input.setEndTime(LocalDateTime.now().plusHours(1)); // invalid

                assertThatThrownBy(() -> appointmentService.bookAppointment(input))
                                .isInstanceOf(InvalidAppointmentWindowException.class);
        }

        @Test
        void bookAppointment_patientServiceCalled() {
                AppointmentDomain input = baseRequest;

                Patient patient = new Patient();
                patient.setId(1L);
                patient.setName(input.getPatient().getName());
                patient.setEmail("test_mail@mail.com");
                
                Doctor doctor = new Doctor();
                doctor.setId(2L);
                Room room = new Room();
                room.setId(3L);

                Appointment saved = new Appointment();
                saved.setId(100L);
                saved.setDoctor(doctor);
                saved.setRoom(room);
                saved.setPatient(patient);
                saved.setStartTime(input.getStartTime());
                saved.setEndTime(input.getEndTime());

                when(patientService.findOrCreatePatient(any(), any())).thenReturn(patient);
                when(doctorService.findAndLockAvailableDoctor(any(), any(), any())).thenReturn(doctor);
                when(roomService.findAndLockAvailableRoom(any(), any())).thenReturn(room);
                when(appointmentRepository.save(any())).thenReturn(saved);

                appointmentService.bookAppointment(input);

                verify(patientService).findOrCreatePatient(patient.getEmail(), patient.getName());
        }

        @Test
        void shouldThrowForInvalidTimeWindow() {
                baseRequest.setEndTime(baseRequest.getStartTime().minusMinutes(30));

                assertThrows(InvalidAppointmentWindowException.class,
                                () -> appointmentService.bookAppointment(baseRequest));
        }

        @Test
        @DisplayName("getAllAppointments should return paginated appointments")
        void getAllAppointments_success() {
                // given
                Doctor doctorEntity = new Doctor();
                doctorEntity.setId(1L);
                doctorEntity.setName("Dr. Strange");
                doctorEntity.setSpecialty("Cardiology");

                Room roomEntity = new Room();
                roomEntity.setId(10L);
                roomEntity.setName("Room A");
                roomEntity.setLocation("First Floor");

                Patient patientEntity = new Patient();
                patientEntity.setName("John Doe");
                patientEntity.setEmail("john.doe@example.com");

                Appointment apptEntity1 = new Appointment();
                apptEntity1.setId(100L);
                apptEntity1.setDoctor(doctorEntity);
                apptEntity1.setRoom(roomEntity);
                apptEntity1.setPatient(patientEntity);
                apptEntity1.setStartTime(LocalDateTime.now().plusHours(1));
                apptEntity1.setEndTime(LocalDateTime.now().plusHours(2));

                Appointment apptEntity2 = new Appointment();
                apptEntity2.setId(101L);
                apptEntity2.setDoctor(doctorEntity);
                apptEntity2.setRoom(roomEntity);
                apptEntity2.setPatient(patientEntity);
                apptEntity2.setStartTime(LocalDateTime.now().plusHours(3));
                apptEntity2.setEndTime(LocalDateTime.now().plusHours(4));

                Page<Appointment> page = new PageImpl<>(List.of(apptEntity1, apptEntity2));

                when(appointmentRepository.findAll(PageRequest.of(0, 20))).thenReturn(page);

                // when
                Page<AppointmentDomain> result = appointmentService.getAllAppointments(PageRequest.of(0, 20));

                // then
                assertThat(result).hasSize(2);
                assertThat(result.getContent().get(0).getId()).isEqualTo(100L);
                assertThat(result.getContent().get(1).getId()).isEqualTo(101L);
        }

        @Test
        @DisplayName("getAllAppointments should return empty list when no appointments exist")
        void getAllAppointments_empty() {
                // given
                Page<Appointment> page = new PageImpl<>(List.of());
                when(appointmentRepository.findAll(PageRequest.of(0, 20))).thenReturn(page);

                // when
                Page<AppointmentDomain> result = appointmentService.getAllAppointments(PageRequest.of(0, 20));

                // then
                assertThat(result).isEmpty();
        }

}
