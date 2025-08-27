package com.uphill.healthcare_booking_system.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import com.uphill.healthcare_booking_system.domain.exceptions.NoAvailableDoctorException;
import com.uphill.healthcare_booking_system.repository.AppointmentRepository;
import com.uphill.healthcare_booking_system.repository.DoctorRepository;
import com.uphill.healthcare_booking_system.repository.entity.Doctor;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @InjectMocks
    private DoctorService doctorService;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    private final String specialty = "Cardiology";
    private final LocalDateTime start = LocalDateTime.now().plusHours(1);
    private final LocalDateTime end = LocalDateTime.now().plusHours(2);

    @Test
    void findAndLockAvailableDoctor_success() {
        Doctor candidate = new Doctor();
        candidate.setId(1L);
        Doctor locked = new Doctor();
        locked.setId(1L);

        when(doctorRepository.findFirstAvailableBySpecialtyAndWindow(eq(specialty), eq(start), eq(end),
                any(PageRequest.class)))
                .thenReturn(List.of(candidate));
        when(doctorRepository.lockById(candidate.getId())).thenReturn(locked);
        when(appointmentRepository.existsOverlapForDoctor(locked.getId(), start, end)).thenReturn(false);

        Doctor result = doctorService.findAndLockAvailableDoctor(specialty, start, end);

        assertThat(result).isEqualTo(locked);
        verify(doctorRepository, times(1)).lockById(candidate.getId());
    }

    @Test
    void findAndLockAvailableDoctor_noDoctorAvailable_throwsException() {
        when(doctorRepository.findFirstAvailableBySpecialtyAndWindow(eq(specialty), eq(start), eq(end),
                any(PageRequest.class)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> doctorService.findAndLockAvailableDoctor(specialty, start, end))
                .isInstanceOf(NoAvailableDoctorException.class);
    }

    @Test
    void findAndLockAvailableDoctor_revalidationFails_throwsException() {
        Doctor candidate = new Doctor();
        candidate.setId(1L);
        Doctor locked = new Doctor();
        locked.setId(1L);

        when(doctorRepository.lockById(candidate.getId())).thenReturn(locked);
        // first check says doctor is busy
        when(appointmentRepository.existsOverlapForDoctor(locked.getId(), start, end)).thenReturn(true);
        // second call: find again
        when(doctorRepository.findFirstAvailableBySpecialtyAndWindow(eq(specialty), eq(start), eq(end),
                any(PageRequest.class)))
                .thenReturn(List.of(candidate));
        when(appointmentRepository.existsOverlapForDoctor(locked.getId(), start, end)).thenReturn(true);

        assertThatThrownBy(() -> doctorService.findAndLockAvailableDoctor(specialty, start, end))
                .isInstanceOf(NoAvailableDoctorException.class);
    }

}
