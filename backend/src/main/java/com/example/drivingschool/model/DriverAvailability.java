package com.example.drivingschool.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "driver_availability")
@Data
public class DriverAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Instructor instructor;

    @Column(name = "availability_month")
    private String month; // Format: "2025-01"
    
    @Column(name = "available_date")
    private LocalDate day; // Specific date
    
    @Column(name = "time_slot")
    private String timeSlot; // Format: "08:00", "09:00", etc.
    
    private String status; // "available", "booked", "locked", "unavailable"

    @Column(name = "is_unavailable_day")
    private boolean isUnavailableDay = false; // For holidays/days off
}

