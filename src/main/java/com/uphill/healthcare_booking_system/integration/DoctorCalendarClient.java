package com.uphill.healthcare_booking_system.integration;

import java.time.LocalDateTime;

public interface DoctorCalendarClient {
    void reserveSlot(Long doctorId, LocalDateTime start, LocalDateTime end);
}
