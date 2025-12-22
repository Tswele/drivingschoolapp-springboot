package com.example.drivingschool.controller;

import com.example.drivingschool.model.Instructor;
import com.example.drivingschool.model.School;
import com.example.drivingschool.model.Review;
import com.example.drivingschool.repository.ReviewRepository;
import com.example.drivingschool.service.SchoolService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schools")
@CrossOrigin
public class SchoolController {
    private final SchoolService schoolService;
    private final ReviewRepository reviewRepository;

    public SchoolController(SchoolService schoolService, ReviewRepository reviewRepository) {
        this.schoolService = schoolService;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping
    public List<School> search(@RequestParam(required = false, name = "city") String city,
                               @RequestParam(required = false, name = "q") String query) {
        return schoolService.search(city, query);
    }

    @GetMapping("/{id}")
    public School detail(@PathVariable("id") Long id) {
        return schoolService.get(id);
    }

    @GetMapping("/{id}/instructors")
    public List<Instructor> instructors(@PathVariable("id") Long id) {
        return schoolService.instructors(id);
    }

    @GetMapping("/{id}/reviews")
    public List<Review> reviews(@PathVariable("id") Long id) {
        School school = schoolService.get(id);
        return reviewRepository.findBySchool(school);
    }
}

