package com.example.drivingschool.repository;

import com.example.drivingschool.model.Review;
import com.example.drivingschool.model.School;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findBySchool(School school);
}

