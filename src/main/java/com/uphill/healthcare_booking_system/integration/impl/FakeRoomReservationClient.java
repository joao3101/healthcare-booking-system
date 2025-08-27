package com.uphill.healthcare_booking_system.integration.impl;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.uphill.healthcare_booking_system.integration.RoomReservationClient;

@Component
public class FakeRoomReservationClient implements RoomReservationClient {
    @Async
    @Override
    public void reserveRoom(Long roomId, LocalDateTime start, LocalDateTime end) {
        System.out.println("Reserved room " + roomId + " from " + start + " to " + end);
    }
}
