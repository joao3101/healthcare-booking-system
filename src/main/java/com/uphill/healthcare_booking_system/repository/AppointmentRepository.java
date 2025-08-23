package com.uphill.healthcare_booking_system.repository;

import com.uphill.healthcare_booking_system.repository.entity.Appointment;
import com.uphill.healthcare_booking_system.repository.entity.Doctor;
import com.uphill.healthcare_booking_system.repository.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.lang.NonNull;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByDoctorAndStartTimeLessThanAndEndTimeGreaterThan(
        Doctor doctor, LocalDateTime end, LocalDateTime start);

    List<Appointment> findByRoomAndStartTimeLessThanAndEndTimeGreaterThan(
        Room room, LocalDateTime end, LocalDateTime start);

    @NonNull
    List<Appointment> findAll();
}
