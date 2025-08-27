package com.uphill.healthcare_booking_system.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.uphill.healthcare_booking_system.domain.exceptions.NoAvailableRoomException;
import com.uphill.healthcare_booking_system.repository.AppointmentRepository;
import com.uphill.healthcare_booking_system.repository.RoomRepository;
import com.uphill.healthcare_booking_system.repository.entity.Room;

@Service
public class RoomService {
    @Autowired private RoomRepository roomRepository;
    @Autowired private AppointmentRepository appointmentRepository;

    // For simplicity reasons, this will return a Room instead of a RoomDomain, but it should return a domain on the service layer
    public Room findAndLockAvailableRoom(LocalDateTime start, LocalDateTime end) {
        Room candidate = roomRepository
                .findFirstAvailableByWindow(start, end, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElseThrow(() -> new NoAvailableRoomException(start, end));

        Room locked = roomRepository.lockById(candidate.getId());

        boolean stillFree = appointmentRepository.existsOverlapForRoom(locked.getId(), start, end);
        if (stillFree) {
            candidate = roomRepository
                    .findFirstAvailableByWindow(start, end, PageRequest.of(0, 1))
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new NoAvailableRoomException(start, end));

            locked = roomRepository.lockById(candidate.getId());
            if (appointmentRepository.existsOverlapForRoom(locked.getId(), start, end)) {
                throw new NoAvailableRoomException(start, end);
            }
        }
        return locked;
    }
}
