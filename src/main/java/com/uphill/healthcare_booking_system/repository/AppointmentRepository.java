package com.uphill.healthcare_booking_system.repository;

import com.uphill.healthcare_booking_system.repository.entity.Appointment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    @Query("""
           SELECT CASE WHEN COUNT(a) > 0 THEN TRUE ELSE FALSE END
           FROM Appointment a
           WHERE a.doctor.id = :doctorId
             AND a.startTime < :end
             AND a.endTime   > :start
           """)
    boolean existsOverlapForDoctor(@Param("doctorId") Long doctorId,
                                   @Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);

    @Query("""
           SELECT CASE WHEN COUNT(a) > 0 THEN TRUE ELSE FALSE END
           FROM Appointment a
           WHERE a.room.id = :roomId
             AND a.startTime < :end
             AND a.endTime   > :start
           """)
    boolean existsOverlapForRoom(@Param("roomId") Long roomId,
                                 @Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end);

    @NonNull
    Page<Appointment> findAll(@NonNull Pageable pageable);
}
