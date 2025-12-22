package com.example.drivingschool.repository;

import com.example.drivingschool.model.Instructor;
import com.example.drivingschool.model.LessonSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LessonSlotRepository extends JpaRepository<LessonSlot, Long> {
    List<LessonSlot> findByInstructorAndStartTimeAfter(Instructor instructor, LocalDateTime start);
    List<LessonSlot> findByInstructor(Instructor instructor);
    Optional<LessonSlot> findByInstructorAndStartTime(Instructor instructor, LocalDateTime startTime);
}

