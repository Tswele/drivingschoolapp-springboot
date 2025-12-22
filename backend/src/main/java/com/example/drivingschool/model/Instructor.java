package com.example.drivingschool.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Instructor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String bio;
    private Double rating;

    @ManyToOne
    private School school;
}

