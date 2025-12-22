package com.example.drivingschool.controller;

import com.example.drivingschool.model.Review;
import com.example.drivingschool.model.School;
import com.example.drivingschool.model.User;
import com.example.drivingschool.repository.ReviewRepository;
import com.example.drivingschool.repository.SchoolRepository;
import com.example.drivingschool.repository.UserRepository;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin
public class ReviewController {
    private final ReviewRepository reviewRepository;
    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;

    public ReviewController(ReviewRepository reviewRepository, SchoolRepository schoolRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.schoolRepository = schoolRepository;
        this.userRepository = userRepository;
    }

    public record CreateReviewRequest(
            @NotNull Long schoolId,
            @NotNull Long userId,
            @Min(1) @Max(5) int rating,
            @NotBlank String comment
    ) {}

    @GetMapping("/school/{id}")
    public List<Review> bySchool(@PathVariable("id") Long id) {
        School school = schoolRepository.findById(id).orElseThrow();
        return reviewRepository.findBySchool(school);
    }

    @PostMapping
    public Review create(@RequestBody CreateReviewRequest request) {
        School school = schoolRepository.findById(request.schoolId()).orElseThrow();
        User user = userRepository.findById(request.userId()).orElseThrow();
        Review review = new Review();
        review.setSchool(school);
        review.setReviewer(user);
        review.setRating(request.rating());
        review.setComment(request.comment());
        return reviewRepository.save(review);
    }
}

