package com.example.drivingschool.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
public class School {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String city;
    private String address;
    private String contactPhone;

    private Double rating;

    @Column(precision = 10, scale = 2)
    private BigDecimal pricePerLesson;

    private Integer defaultLessonMinutes;
}

