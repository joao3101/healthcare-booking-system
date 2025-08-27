package com.uphill.healthcare_booking_system.integration.impl;

import org.springframework.stereotype.Component;

import com.uphill.healthcare_booking_system.integration.EmailClient;

@Component
public class FakeEmailClient implements EmailClient {
    @Override
    public void sendAppointmentConfirmation(String toEmail, String subject, String body) {
        System.out.println("Sent email to " + toEmail + " with subject: " + subject);
    }
}
