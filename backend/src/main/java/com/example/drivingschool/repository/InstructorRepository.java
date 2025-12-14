package com.example.drivingschool.repository;

import com.example.drivingschool.model.Instructor;
import com.example.drivingschool.model.School;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {
    List<Instructor> findBySchool(School school);
}

