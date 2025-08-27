package com.uphill.healthcare_booking_system.controller.input;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AppointmentInput {
    @NotBlank(message = "Patient email is required")
    @Email(message = "Patient email must be valid")
    @JsonProperty("patient_email")
    private String patientEmail;

    @NotBlank(message = "Patient name is required")
    @JsonProperty("patient_name")
    private String patientName;

    @NotNull(message = "Start date is required")
    @JsonProperty("start_date")
    private Long startDate;

    @NotNull(message = "End date is required")
    @JsonProperty("end_date")
    private Long endDate;

    @NotBlank(message = "Specialty is required")
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
