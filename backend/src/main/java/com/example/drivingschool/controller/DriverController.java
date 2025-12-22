package com.example.drivingschool.controller;

import com.example.drivingschool.model.*;
import com.example.drivingschool.repository.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/driver")
@CrossOrigin
public class DriverController {

    private final InstructorRepository instructorRepository;
    private final BookingRepository bookingRepository;
    private final DriverAvailabilityRepository driverAvailabilityRepository;
    private final LessonSlotRepository slotRepository;

    public DriverController(InstructorRepository instructorRepository,
                           BookingRepository bookingRepository,
                           DriverAvailabilityRepository driverAvailabilityRepository,
                           LessonSlotRepository slotRepository) {
        this.instructorRepository = instructorRepository;
        this.bookingRepository = bookingRepository;
        this.driverAvailabilityRepository = driverAvailabilityRepository;
        this.slotRepository = slotRepository;
    }

    @GetMapping("/{instructorId}/bookings")
    public List<Booking> getBookings(@PathVariable("instructorId") Long instructorId) {
        Instructor instructor = instructorRepository.findById(instructorId).orElseThrow();
        List<LessonSlot> slots = slotRepository.findByInstructor(instructor);
        return bookingRepository.findAll().stream()
                .filter(b -> slots.contains(b.getSlot()))
                .collect(Collectors.toList());
    }

    @GetMapping("/{instructorId}/bookings/day/{date}")
    public List<Booking> getBookingsByDay(@PathVariable("instructorId") Long instructorId, 
                                          @PathVariable("date") String date) {
        LocalDate day = LocalDate.parse(date);
        Instructor instructor = instructorRepository.findById(instructorId).orElseThrow();
        return bookingRepository.findAll().stream()
                .filter(b -> b.getSlot().getInstructor().equals(instructor))
                .filter(b -> b.getSlot().getStartTime().toLocalDate().equals(day))
                .collect(Collectors.toList());
    }

    @PostMapping("/bookings/{bookingId}/confirm")
    public Booking confirmBooking(@PathVariable("bookingId") Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.getSlot().setAvailable(false);
        
        // Update driver availability
        LocalDate day = booking.getSlot().getStartTime().toLocalDate();
        String timeSlot = booking.getSlot().getStartTime().toLocalTime().toString().substring(0, 5);
        List<DriverAvailability> availabilities = driverAvailabilityRepository
                .findByInstructorAndDayAndTimeSlot(booking.getSlot().getInstructor(), day, timeSlot);
        for (DriverAvailability av : availabilities) {
            av.setStatus("locked");
        }
        driverAvailabilityRepository.saveAll(availabilities);
        
        return bookingRepository.save(booking);
    }

    @PostMapping("/bookings/{bookingId}/reject")
    public Booking rejectBooking(@PathVariable("bookingId") Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        booking.setStatus(BookingStatus.CANCELLED);
        booking.getSlot().setAvailable(true);
        
        // Update driver availability back to available
        LocalDate day = booking.getSlot().getStartTime().toLocalDate();
        String timeSlot = booking.getSlot().getStartTime().toLocalTime().toString().substring(0, 5);
        List<DriverAvailability> availabilities = driverAvailabilityRepository
                .findByInstructorAndDayAndTimeSlot(booking.getSlot().getInstructor(), day, timeSlot);
        for (DriverAvailability av : availabilities) {
            av.setStatus("available");
        }
        driverAvailabilityRepository.saveAll(availabilities);
        
        return bookingRepository.save(booking);
    }

    @GetMapping("/{instructorId}/calendar/{month}")
    public List<DriverAvailability> getCalendar(@PathVariable("instructorId") Long instructorId,
                                                @PathVariable("month") String month) {
        Instructor instructor = instructorRepository.findById(instructorId).orElseThrow();
        return driverAvailabilityRepository.findByInstructorAndMonth(instructor, month);
    }
}

