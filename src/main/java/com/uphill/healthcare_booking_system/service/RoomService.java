package com.uphill.healthcare_booking_system.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(RoomService.class);

    // For simplicity reasons, this will return a Room instead of a RoomDomain, but it should return a domain on the service layer
    public Room findAndLockAvailableRoom(LocalDateTime start, LocalDateTime end) {
        Room candidate = roomRepository
                .findFirstAvailableByWindow(start, end, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElseThrow(() -> new NoAvailableRoomException(start, end));

        Room locked = roomRepository.lockById(candidate.getId());
        log.info("Room locked: id={}, name={}", locked.getId(), locked.getName());

        // This was an improvement suggested by ChatGPT in order to confirm that the room is still available for the appointment
        boolean stillFree = appointmentRepository.existsOverlapForRoom(locked.getId(), start, end);
        if (stillFree) {
            candidate = roomRepository
                    .findFirstAvailableByWindow(start, end, PageRequest.of(0, 1))
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new NoAvailableRoomException(start, end));

            locked = roomRepository.lockById(candidate.getId());
            log.info("Room locked: id={}, name={}", locked.getId(), locked.getName());
            if (appointmentRepository.existsOverlapForRoom(locked.getId(), start, end)) {
                log.error("Room is still free for appointment id={}", locked.getId());
                throw new NoAvailableRoomException(start, end);
            }
        }
        return locked;
    }
}
