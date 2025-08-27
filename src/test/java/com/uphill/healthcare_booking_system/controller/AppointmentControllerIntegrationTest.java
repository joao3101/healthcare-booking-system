package com.uphill.healthcare_booking_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uphill.healthcare_booking_system.controller.input.AppointmentInput;
import com.uphill.healthcare_booking_system.domain.AppointmentDomain;
import com.uphill.healthcare_booking_system.domain.DoctorDomain;
import com.uphill.healthcare_booking_system.domain.PatientDomain;
import com.uphill.healthcare_booking_system.domain.RoomDomain;
import com.uphill.healthcare_booking_system.domain.exceptions.InvalidAppointmentWindowException;
import com.uphill.healthcare_booking_system.domain.exceptions.NoAvailableDoctorException;
import com.uphill.healthcare_booking_system.domain.exceptions.NoAvailableRoomException;
import com.uphill.healthcare_booking_system.service.AppointmentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AppointmentController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AppointmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppointmentService appointmentService;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public AppointmentService appointmentService() {
            return Mockito.mock(AppointmentService.class);
        }
    }

    @BeforeEach
    void resetMocks() {
        Mockito.reset(appointmentService);
    }

    private AppointmentInput buildValidInput() {
        AppointmentInput input = new AppointmentInput();
        input.setStartDate(1693046400L);
        input.setEndDate(1693050000L);
        input.setSpecialty("Cardiology");
        input.setPatientName("John Doe");
        input.setPatientEmail("john.doe@example.com");
        return input;
    }

    @Test
    void bookAppointment_success() throws Exception {
        AppointmentInput input = buildValidInput();

        DoctorDomain doctor = new DoctorDomain();
        doctor.setId(1L);
        doctor.setName("Dr. Strange");
        doctor.setSpecialty("Cardiology");

        RoomDomain room = new RoomDomain();
        room.setId(10L);
        room.setName("Room A");
        room.setLocation("First Floor");

        PatientDomain patient = new PatientDomain();
        patient.setName("John Doe");
        patient.setEmail("john.doe@example.com");

        AppointmentDomain domain = new AppointmentDomain();
        domain.setId(100L);
        domain.setStartTime(LocalDateTime.now().plusHours(1));
        domain.setEndTime(LocalDateTime.now().plusHours(2));
        domain.setDoctor(doctor);
        domain.setRoom(room);
        domain.setPatient(patient);

        when(appointmentService.bookAppointment(any())).thenReturn(domain);

        mockMvc.perform(post("/v1/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentId").value(100L))
                .andExpect(jsonPath("$.doctorId").value(1L))
                .andExpect(jsonPath("$.roomId").value(10L));
    }

    @Test
    void bookAppointment_noAvailableDoctor() throws Exception {
        AppointmentInput input = buildValidInput();

        when(appointmentService.bookAppointment(any()))
                .thenThrow(new NoAvailableDoctorException(
                        "Cardiology",
                        LocalDateTime.now().plusHours(1),
                        LocalDateTime.now().plusHours(2)));

        mockMvc.perform(post("/v1/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.exception").value("NoAvailableDoctorException"));
    }

    @Test
    void bookAppointment_noAvailableRoom() throws Exception {
        AppointmentInput input = buildValidInput();

        when(appointmentService.bookAppointment(any()))
                .thenThrow(new NoAvailableRoomException(
                        LocalDateTime.now().plusHours(1),
                        LocalDateTime.now().plusHours(2)));

        mockMvc.perform(post("/v1/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.exception").value("NoAvailableRoomException"));
    }

    @Test
    void bookAppointment_invalidWindow() throws Exception {
        AppointmentInput input = buildValidInput();

        when(appointmentService.bookAppointment(any()))
                .thenThrow(new InvalidAppointmentWindowException());

        mockMvc.perform(post("/v1/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.exception").value("InvalidAppointmentWindowException"));
    }

    @Test
    void bookAppointment_genericError() throws Exception {
        AppointmentInput input = buildValidInput();

        when(appointmentService.bookAppointment(any()))
                .thenThrow(new RuntimeException("Unexpected failure"));

        mockMvc.perform(post("/v1/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.exception").value("RuntimeException"));
    }

    @Test
    @DisplayName("GET /v1/appointments should return paginated appointments")
    void getAllAppointments_paginated_success() throws Exception {
        // given
        DoctorDomain doctor = new DoctorDomain();
        doctor.setId(1L);
        doctor.setName("Dr. Strange");
        doctor.setSpecialty("Cardiology");

        RoomDomain room = new RoomDomain();
        room.setId(10L);
        room.setName("Room A");
        room.setLocation("First Floor");

        PatientDomain patient = new PatientDomain();
        patient.setName("John Doe");
        patient.setEmail("john.doe@example.com");

        AppointmentDomain appt1 = new AppointmentDomain();
        appt1.setId(100L);
        appt1.setStartTime(LocalDateTime.now().plusHours(1));
        appt1.setEndTime(LocalDateTime.now().plusHours(2));
        appt1.setDoctor(doctor);
        appt1.setRoom(room);
        appt1.setPatient(patient);

        AppointmentDomain appt2 = new AppointmentDomain();
        appt2.setId(101L);
        appt2.setStartTime(LocalDateTime.now().plusHours(3));
        appt2.setEndTime(LocalDateTime.now().plusHours(4));
        appt2.setDoctor(doctor);
        appt2.setRoom(room);
        appt2.setPatient(patient);

        when(appointmentService.getAllAppointments(0, 20))
                .thenReturn(List.of(appt1, appt2));

        // when & then
        mockMvc.perform(get("/v1/appointments")
                .param("page", "0")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].appointmentId").value(100L))
                .andExpect(jsonPath("$[1].appointmentId").value(101L));
    }

    @Test
    @DisplayName("GET /v1/appointments should return empty list when no appointments exist")
    void getAllAppointments_paginated_empty() throws Exception {
        // given
        when(appointmentService.getAllAppointments(0, 20)).thenReturn(List.of());

        // when & then
        mockMvc.perform(get("/v1/appointments")
                .param("page", "0")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /v1/appointments should return 500 when service throws exception")
    void getAllAppointments_paginated_serviceError() throws Exception {
        // given
        when(appointmentService.getAllAppointments(0, 20))
                .thenThrow(new RuntimeException("Unexpected error"));

        // when & then
        mockMvc.perform(get("/v1/appointments")
                .param("page", "0")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Unexpected error"));
    }
}
