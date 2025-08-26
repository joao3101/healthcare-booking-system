package com.uphill.healthcare_booking_system.controller.input;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AppointmentInput {
    @JsonProperty("patient_email")
    private String patientEmail;

    @JsonProperty("patient_name")
    private String patientName;

    @JsonProperty("start_date")
    private Long startDate;

    @JsonProperty("end_date")
    private Long endDate;

    @JsonProperty("specialty")
    private String specialty;

    public String getPatientEmail() {
        return patientEmail;
    }
    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }
    public String getPatientName() {
        return patientName;
    }
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
    public Long getStartDate() {
        return startDate;
    }
    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }
    public Long getEndDate() {
        return endDate;
    }
    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }
    public String getSpecialty() {
        return specialty;
    }
    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }
}
