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

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AppointmentController.class)
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
}
