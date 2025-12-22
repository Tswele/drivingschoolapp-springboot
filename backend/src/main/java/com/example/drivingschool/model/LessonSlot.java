package com.example.drivingschool.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
public class LessonSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startTime;
    private Integer durationMinutes;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    private boolean available = true;

    @ManyToOne
    private Instructor instructor;
}

