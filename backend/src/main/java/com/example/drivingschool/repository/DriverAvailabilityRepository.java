package com.example.drivingschool.repository;

import com.example.drivingschool.model.DriverAvailability;
import com.example.drivingschool.model.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DriverAvailabilityRepository extends JpaRepository<DriverAvailability, Long> {
    List<DriverAvailability> findByInstructor(Instructor instructor);
    List<DriverAvailability> findByInstructorAndMonth(Instructor instructor, String month);
    List<DriverAvailability> findByInstructorAndDay(Instructor instructor, LocalDate day);
    List<DriverAvailability> findByInstructorAndDayAndTimeSlot(Instructor instructor, LocalDate day, String timeSlot);
    List<DriverAvailability> findByInstructorAndMonthAndStatus(Instructor instructor, String month, String status);
}

