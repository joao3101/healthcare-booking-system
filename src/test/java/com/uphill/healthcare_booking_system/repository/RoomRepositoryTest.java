package com.uphill.healthcare_booking_system.repository;

import com.uphill.healthcare_booking_system.repository.entity.Room;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    @Test
    void testSaveAndFindById() {
        Room room = new Room();
        room.setName("Room A");

        Room saved = roomRepository.save(room);

        Optional<Room> found = roomRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Room A");
    }

    @Test
    void testFindAll() {
        Room r1 = new Room();
        r1.setName("Room B");

        Room r2 = new Room();
        r2.setName("Room C");

        roomRepository.saveAll(List.of(r1, r2));

        List<Room> rooms = roomRepository.findAll();

        assertThat(rooms).hasSize(2)
                .extracting(Room::getName)
                .containsExactlyInAnyOrder("Room B", "Room C");
    }

    @Test
    void testDelete() {
        Room room = new Room();
        room.setName("Room D");
        Room saved = roomRepository.save(room);

        roomRepository.delete(saved);

        assertThat(roomRepository.findById(saved.getId())).isNotPresent();
    }
}
