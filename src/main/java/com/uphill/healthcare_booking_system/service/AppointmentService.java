package com.uphill.healthcare_booking_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import com.uphill.healthcare_booking_system.domain.AppointmentDomain;
import com.uphill.healthcare_booking_system.domain.DoctorDomain;
import com.uphill.healthcare_booking_system.domain.RoomDomain;
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

import jakarta.transaction.Transactional;

public class AppointmentService {
    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Transactional
    public AppointmentDomain bookAppointment(AppointmentDomain req) {      
        if (req.getStartTime() == null || req.getEndTime() == null || !req.getEndTime().isAfter(req.getStartTime())) {
            throw new InvalidAppointmentWindowException();
        }
        final var start = req.getStartTime();
        final var end = req.getEndTime();
        final var specialty = req.getSpecialty();

        Patient patient = patientRepository.findByEmail(req.getPatient().getEmail());
        if (patient == null) {
            patient = new Patient();
            patient.setName(req.getPatient().getName());
            patient.setEmail(req.getPatient().getEmail());
            patientRepository.save(patient);
        }

        Doctor candidateDoctor = doctorRepository
                .findFirstAvailableBySpecialtyAndWindow(specialty, start, end, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElseThrow(() -> new NoAvailableDoctorException(specialty, start, end));

        Doctor lockedDoctor = doctorRepository.lockById(candidateDoctor.getId());
        // This revalidation was added after checking for possible implementations
        // issues with ChatGTP;
        boolean doctorStillFree = appointmentRepository
                .existsOverlapForDoctor(lockedDoctor.getId(), start, end);
        if (doctorStillFree) {
            candidateDoctor = doctorRepository
                    .findFirstAvailableBySpecialtyAndWindow(specialty, start, end, PageRequest.of(0, 1))
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new NoAvailableDoctorException(specialty, start, end));
            lockedDoctor = doctorRepository.lockById(candidateDoctor.getId());
            doctorStillFree = appointmentRepository.existsOverlapForDoctor(lockedDoctor.getId(), start, end);
            if (doctorStillFree) {
                throw new NoAvailableDoctorException(specialty, start, end);
            }
        }

        Room candidateRoom = roomRepository
                .findFirstAvailableByWindow(start, end, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElseThrow(() -> new NoAvailableRoomException(start, end));

        Room lockedRoom = roomRepository.lockById(candidateRoom.getId());
        // This revalidation was added after checking for possible implementations
        // issues with ChatGTP;
        boolean roomStillFree = appointmentRepository
                .existsOverlapForRoom(lockedRoom.getId(), start, end);
        if (roomStillFree) {
            candidateRoom = roomRepository
                    .findFirstAvailableByWindow(start, end, PageRequest.of(0, 1))
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new NoAvailableRoomException(start, end));
            lockedRoom = roomRepository.lockById(candidateRoom.getId());
            roomStillFree = appointmentRepository.existsOverlapForRoom(lockedRoom.getId(), start, end);
            if (roomStillFree) {
                throw new NoAvailableRoomException(start, end);
            }
        }

        Appointment appt = new Appointment();
        appt.setDoctor(lockedDoctor);
        appt.setRoom(lockedRoom);
        appt.setPatient(patient);
        appt.setStartTime(start);
        appt.setEndTime(end);

        Appointment savedAppointment = appointmentRepository.save(appt);

        DoctorDomain doctorDomain = new DoctorDomain();
        doctorDomain.setId(savedAppointment.getDoctor().getId());
        doctorDomain.setName(savedAppointment.getDoctor().getName());
        doctorDomain.setSpecialty(savedAppointment.getDoctor().getSpecialty());

        RoomDomain roomDomain = new RoomDomain();
        roomDomain.setId(savedAppointment.getRoom().getId());
        roomDomain.setName(savedAppointment.getRoom().getName());
        roomDomain.setLocation(savedAppointment.getRoom().getLocation());

        AppointmentDomain savedDomain = new AppointmentDomain();
        savedDomain.setId(savedAppointment.getId());
        savedDomain.setDoctor(doctorDomain);
        savedDomain.setRoom(roomDomain);
        savedDomain.setStartTime(start);
        savedDomain.setEndTime(end);

        return savedDomain;
    }
}
