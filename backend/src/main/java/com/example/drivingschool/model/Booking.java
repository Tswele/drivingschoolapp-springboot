package com.example.drivingschool.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User learner;

    @ManyToOne
    private LessonSlot slot;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();

    private String paymentMethod; // CASH or CARD (stored as uppercase string)
    private String cardLast4;     // last 4 digits if card was used
}

