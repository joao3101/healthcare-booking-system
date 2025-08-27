package com.uphill.healthcare_booking_system.integration.impl;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.uphill.healthcare_booking_system.integration.DoctorCalendarClient;

@Component
public class FakeDoctorCalendarClient implements DoctorCalendarClient {
    @Async
    @Override
    public void reserveSlot(Long doctorId, LocalDateTime start, LocalDateTime end) {
        // Apenas log para simulação
        System.out.println("Reserved doctor " + doctorId + " from " + start + " to " + end);
    }
}
