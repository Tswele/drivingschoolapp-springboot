package com.example.drivingschool.repository;

import com.example.drivingschool.model.School;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchoolRepository extends JpaRepository<School, Long> {
    List<School> findByCityIgnoreCase(String city);
    List<School> findByNameContainingIgnoreCase(String name);
}

