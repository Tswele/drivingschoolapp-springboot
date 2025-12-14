package com.example.drivingschool.controller;

import com.example.drivingschool.model.Booking;
import com.example.drivingschool.model.LessonSlot;
import com.example.drivingschool.service.BookingService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/instructors/{id}/slots")
    public List<LessonSlot> slots(@PathVariable("id") Long id) {
        return bookingService.availableSlots(id);
    }

    public record CreateBookingRequest(
            Long userId,
            @NotNull Long slotId,
            String fullName,
            String email,
            String phone,
            String paymentMethod,
            String cardLast4
    ) {}

    public record CreateDriverAvailabilityBookingRequest(
            Long userId,
            Long instructorId,
            String date, // YYYY-MM-DD
            String timeSlot, // HH:MM
            String fullName,
            String email,
            String phone,
            String paymentMethod,
            String cardLast4
    ) {}

    @PostMapping("/bookings")
    public Booking create(@RequestBody CreateBookingRequest request) {
        return bookingService.createBooking(request);
    }

    @PostMapping("/bookings/driver-flow")
    public Booking createPending(@RequestBody CreateBookingRequest request) {
        return bookingService.createPendingBooking(request);
    }

    @PostMapping("/bookings/driver-availability")
    public ResponseEntity<?> createFromDriverAvailability(@RequestBody CreateDriverAvailabilityBookingRequest request) {
        try {
            Booking booking = bookingService.createBookingFromDriverAvailability(request);
            return ResponseEntity.ok(booking);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
        } catch (Exception e) {
            e.printStackTrace(); // Log the full stack trace
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return ResponseEntity.status(500).body("{\"error\": \"Internal server error: " + errorMsg.replace("\"", "\\\"") + "\"}");
        }
    }

    @PostMapping("/bookings/{id}/cancel")
    public Booking cancel(@PathVariable("id") Long id) {
        return bookingService.cancelBooking(id);
    }

    @GetMapping("/bookings")
    public List<Booking> all() {
        return bookingService.allBookings();
    }

    @GetMapping("/users/{id}/bookings")
    public List<Booking> userBookings(@PathVariable("id") Long id) {
        return bookingService.bookingsForUser(id);
    }
}

