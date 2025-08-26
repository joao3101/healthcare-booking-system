package com.uphill.healthcare_booking_system.controller.input;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AppointmentInput {
    @JsonProperty("start_date")
    private Long startDate;
    @JsonProperty("end_date")
    private Long endDate;
    @JsonProperty("specialty")
    private String specialty;

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
