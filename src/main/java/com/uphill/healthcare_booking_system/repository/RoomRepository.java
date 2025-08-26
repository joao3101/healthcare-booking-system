package com.uphill.healthcare_booking_system.repository;

import com.uphill.healthcare_booking_system.repository.entity.Room;

import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomRepository extends JpaRepository<Room, Long> {
    @Query("""
           SELECT r FROM Room r
           WHERE NOT EXISTS (
               SELECT a.id FROM Appointment a
               WHERE a.room = r
                 AND a.startTime < :end
                 AND a.endTime   > :start
           )
           ORDER BY r.id ASC
           """)
    List<Room> findFirstAvailableByWindow(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end,
                                          Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.id = :id")
    Room lockById(@Param("id") Long id);
}
