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

import com.uphill.healthcare_booking_system.domain.exceptions.NoAvailableRoomException;
import com.uphill.healthcare_booking_system.repository.AppointmentRepository;
import com.uphill.healthcare_booking_system.repository.RoomRepository;
import com.uphill.healthcare_booking_system.repository.entity.Room;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @InjectMocks
    private RoomService roomService;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    private final LocalDateTime start = LocalDateTime.now().plusHours(1);
    private final LocalDateTime end = LocalDateTime.now().plusHours(2);

    @Test
    void findAndLockAvailableRoom_success() {
        Room candidate = new Room();
        candidate.setId(10L);
        Room locked = new Room();
        locked.setId(10L);

        when(roomRepository.findFirstAvailableByWindow(eq(start), eq(end), any(PageRequest.class)))
                .thenReturn(List.of(candidate));
        when(roomRepository.lockById(candidate.getId())).thenReturn(locked);
        when(appointmentRepository.existsOverlapForRoom(locked.getId(), start, end)).thenReturn(false);

        Room result = roomService.findAndLockAvailableRoom(start, end);

        assertThat(result).isEqualTo(locked);
        verify(roomRepository, times(1)).lockById(candidate.getId());
    }

    @Test
    void findAndLockAvailableRoom_noRoomAvailable_throwsException() {
        when(roomRepository.findFirstAvailableByWindow(eq(start), eq(end), any(PageRequest.class)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> roomService.findAndLockAvailableRoom(start, end))
                .isInstanceOf(NoAvailableRoomException.class);
    }

    @Test
    void findAndLockAvailableRoom_revalidationFails_throwsException() {
        Room candidate = new Room();
        candidate.setId(10L);
        Room locked = new Room();
        locked.setId(10L);

        when(roomRepository.lockById(candidate.getId())).thenReturn(locked);
        // first check says room is busy
        when(appointmentRepository.existsOverlapForRoom(locked.getId(), start, end)).thenReturn(true);
        // second call: find again
        when(roomRepository.findFirstAvailableByWindow(eq(start), eq(end), any(PageRequest.class)))
                .thenReturn(List.of(candidate));
        when(appointmentRepository.existsOverlapForRoom(locked.getId(), start, end)).thenReturn(true);

        assertThatThrownBy(() -> roomService.findAndLockAvailableRoom(start, end))
                .isInstanceOf(NoAvailableRoomException.class);
    }
}
