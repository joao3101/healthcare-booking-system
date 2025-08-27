package com.uphill.healthcare_booking_system;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.uphill.healthcare_booking_system.repository.DoctorRepository;
import com.uphill.healthcare_booking_system.repository.RoomRepository;
import com.uphill.healthcare_booking_system.repository.entity.Doctor;
import com.uphill.healthcare_booking_system.repository.entity.Room;

@SpringBootApplication
public class HealthcareBookingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(HealthcareBookingSystemApplication.class, args);
	}

	@Bean
    CommandLineRunner initData(DoctorRepository doctorRepo, RoomRepository roomRepo) {
        return args -> {
            if (doctorRepo.count() == 0) {
				Doctor drStrange = new Doctor();
				drStrange.setName("Dra. Strange");
				drStrange.setSpecialty("Cardiology");

				Doctor drHouse = new Doctor();
				drHouse.setName("Dr. House");
				drHouse.setSpecialty("Neurology");

				Doctor drWho = new Doctor();
				drWho.setName("Dr. Who");
				drWho.setSpecialty("Pediatrics");

                doctorRepo.saveAll(List.of(drStrange, drHouse, drWho));
            }

            if (roomRepo.count() == 0) {
				Room roomA = new Room();
				roomA.setName("Room A");
				roomA.setLocation("First Floor");

				Room roomB = new Room();
				roomB.setName("Room B");
				roomB.setLocation("Second Floor");

				Room roomC = new Room();
				roomC.setName("Room C");
				roomC.setLocation("Third Floor");

                roomRepo.saveAll(List.of(roomA, roomB, roomC));
            }
        };
    }

}
