package com.uphill.healthcare_booking_system.integration;

public interface EmailClient {
    void sendAppointmentConfirmation(String toEmail, String subject, String body);
}
