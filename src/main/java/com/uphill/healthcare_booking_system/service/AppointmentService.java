package com.uphill.healthcare_booking_system.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.uphill.healthcare_booking_system.domain.AppointmentDomain;
import com.uphill.healthcare_booking_system.domain.DoctorDomain;
import com.uphill.healthcare_booking_system.domain.PatientDomain;
import com.uphill.healthcare_booking_system.domain.RoomDomain;
import com.uphill.healthcare_booking_system.domain.exceptions.InvalidAppointmentWindowException;
import com.uphill.healthcare_booking_system.enums.AppointmentStatus;
import com.uphill.healthcare_booking_system.repository.AppointmentRepository;
import com.uphill.healthcare_booking_system.repository.entity.Appointment;
import com.uphill.healthcare_booking_system.repository.entity.Doctor;
import com.uphill.healthcare_booking_system.repository.entity.Patient;
import com.uphill.healthcare_booking_system.repository.entity.Room;

import com.uphill.healthcare_booking_system.integration.DoctorCalendarClient;
import com.uphill.healthcare_booking_system.integration.EmailClient;
import com.uphill.healthcare_booking_system.integration.RoomReservationClient;

import jakarta.transaction.Transactional;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientService patientService;
    private final DoctorCalendarClient doctorCalendarClient;
    private final RoomReservationClient roomReservationClient;
    private final EmailClient emailClient;
    private final DoctorService doctorService;
    private final RoomService roomService;

    public AppointmentService(AppointmentRepository appointmentRepository,
            PatientService patientService,
            DoctorCalendarClient doctorCalendarClient,
            RoomReservationClient roomReservationClient,
            EmailClient emailClient,
            DoctorService doctorService,
            RoomService roomService) {
        this.appointmentRepository = appointmentRepository;
        this.patientService = patientService;
        this.doctorCalendarClient = doctorCalendarClient;
        this.roomReservationClient = roomReservationClient;
        this.emailClient = emailClient;
        this.doctorService = doctorService;
        this.roomService = roomService;
    }

    @Transactional
    public AppointmentDomain bookAppointment(AppointmentDomain req) {
        final var start = req.getStartTime();
        final var end = req.getEndTime();
        
        checkTimeWindow(start, end);

        // Best scenario is to pass a PatientDomain object to the service, passing only those two params for simplification purposes
        Patient patient = patientService.findOrCreatePatient(req.getPatient().getEmail(), req.getPatient().getName());

        Doctor doctor = doctorService.findAndLockAvailableDoctor(req.getSpecialty(), start, end);
        Room room = roomService.findAndLockAvailableRoom(start, end);

        // This can be refactored to a mapper method
        Appointment appt = new Appointment();
        appt.setDoctor(doctor);
        appt.setRoom(room);
        appt.setPatient(patient);
        appt.setStatus(AppointmentStatus.SCHEDULED);
        appt.setStartTime(start);
        appt.setEndTime(end);

        Appointment savedAppointment = appointmentRepository.save(appt);

        AppointmentDomain domain = convertToDomain(savedAppointment);

        updateDoctorCalendar(domain);
        reserveRoom(domain);
        sendConfirmationEmail(domain);

        return domain;
    }

    public List<AppointmentDomain> getAllAppointments(int page, int size) {
        Page<Appointment> appointmentsPage = appointmentRepository.findAll(PageRequest.of(page, size));
        return appointmentsPage.stream()
                .map(this::convertToDomain)
                .collect(Collectors.toList());
    }

    private void checkTimeWindow(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !end.isAfter(start) || !start.isAfter(LocalDateTime.now())) {
            throw new InvalidAppointmentWindowException();
        }
    }

    private AppointmentDomain convertToDomain(Appointment appointment) {
        DoctorDomain doctorDomain = new DoctorDomain();
        doctorDomain.setId(appointment.getDoctor().getId());
        doctorDomain.setName(appointment.getDoctor().getName());
        doctorDomain.setSpecialty(appointment.getDoctor().getSpecialty());

        RoomDomain roomDomain = new RoomDomain();
        roomDomain.setId(appointment.getRoom().getId());
        roomDomain.setName(appointment.getRoom().getName());
        roomDomain.setLocation(appointment.getRoom().getLocation());

        PatientDomain patientDomain = new PatientDomain();
        patientDomain.setId(appointment.getPatient().getId());
        patientDomain.setName(appointment.getPatient().getName());
        patientDomain.setEmail(appointment.getPatient().getEmail());

        AppointmentDomain domain = new AppointmentDomain();
        domain.setId(appointment.getId());
        domain.setDoctor(doctorDomain);
        domain.setRoom(roomDomain);
        domain.setPatient(patientDomain);
        domain.setStatus(appointment.getStatus());
        domain.setStartTime(appointment.getStartTime());
        domain.setEndTime(appointment.getEndTime());
        return domain;
    }

    private void updateDoctorCalendar(AppointmentDomain appointment) {
        doctorCalendarClient.reserveSlot(
                appointment.getDoctor().getId(),
                appointment.getStartTime(),
                appointment.getEndTime());
    }

    private void reserveRoom(AppointmentDomain appointment) {
        roomReservationClient.reserveRoom(
                appointment.getRoom().getId(),
                appointment.getStartTime(),
                appointment.getEndTime());
    }

    private void sendConfirmationEmail(AppointmentDomain appointment) {
        String subject = "Appointment confirmed";
        String body = String.format("Dear %s, your appointment with Dr. %s is confirmed for %s - %s",
                appointment.getPatient().getName(),
                appointment.getDoctor().getName(),
                appointment.getStartTime(),
                appointment.getEndTime());
        emailClient.sendAppointmentConfirmation(appointment.getPatient().getEmail(), subject, body);
    }
}
