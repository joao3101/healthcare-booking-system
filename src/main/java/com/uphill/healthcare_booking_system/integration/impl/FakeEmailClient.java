package com.uphill.healthcare_booking_system.integration.impl;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.uphill.healthcare_booking_system.integration.EmailClient;

@Component
public class FakeEmailClient implements EmailClient {
    @Async
    @Override
    public void sendAppointmentConfirmation(String toEmail, String subject, String body) {
        System.out.println("Sent email to " + toEmail + " with subject: " + subject);
        System.out.println("Body: " + body);
    }
}
