package com.example.drivingschool.repository;

import com.example.drivingschool.model.Booking;
import com.example.drivingschool.model.BookingStatus;
import com.example.drivingschool.model.LessonSlot;
import com.example.drivingschool.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByLearner(User learner);
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findBySlot(LessonSlot slot);
    void deleteBySlot(LessonSlot slot);
}

