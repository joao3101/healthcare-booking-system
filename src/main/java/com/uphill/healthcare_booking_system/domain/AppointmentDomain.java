package com.uphill.healthcare_booking_system.domain;

import java.time.LocalDateTime;

public class AppointmentDomain {

    private Long id;
    private String specialty;
    private PatientDomain patient;
    private DoctorDomain doctor;
    private RoomDomain room;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public PatientDomain getPatient() {
        return patient;
    }

    public void setPatient(PatientDomain patient) {
        this.patient = patient;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public DoctorDomain getDoctor() {
        return doctor;
    }

    public void setDoctor(DoctorDomain doctor) {
        this.doctor = doctor;
    }

    public RoomDomain getRoom() {
        return room;
    }

    public void setRoom(RoomDomain room) {
        this.room = room;
    }

}
