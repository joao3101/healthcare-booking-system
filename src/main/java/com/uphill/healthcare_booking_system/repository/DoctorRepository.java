package com.uphill.healthcare_booking_system.repository;

import com.uphill.healthcare_booking_system.repository.entity.Doctor;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    @Query("""
           SELECT d FROM Doctor d
           WHERE d.specialty = :specialty
             AND NOT EXISTS (
                 SELECT a.id FROM Appointment a
                 WHERE a.doctor = d
                   AND a.startTime < :end
                   AND a.endTime   > :start
             )
           ORDER BY d.id ASC
           """)
    List<Doctor> findFirstAvailableBySpecialtyAndWindow(@Param("specialty") String specialty,
                                                        @Param("start") LocalDateTime start,
                                                        @Param("end") LocalDateTime end,
                                                        Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Doctor d WHERE d.id = :id")
    Doctor lockById(@Param("id") Long id);
}
