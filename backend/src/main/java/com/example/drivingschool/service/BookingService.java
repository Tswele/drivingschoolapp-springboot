package com.example.drivingschool.service;

import com.example.drivingschool.controller.BookingController;
import com.example.drivingschool.model.*;
import com.example.drivingschool.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final LessonSlotRepository slotRepository;
    private final InstructorRepository instructorRepository;
    private final DriverAvailabilityRepository driverAvailabilityRepository;

    public BookingService(BookingRepository bookingRepository,
                          UserRepository userRepository,
                          LessonSlotRepository slotRepository,
                          InstructorRepository instructorRepository,
                          DriverAvailabilityRepository driverAvailabilityRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.slotRepository = slotRepository;
        this.instructorRepository = instructorRepository;
        this.driverAvailabilityRepository = driverAvailabilityRepository;
    }

    public List<LessonSlot> availableSlots(Long instructorId) {
        Instructor instructor = instructorRepository.findById(instructorId).orElseThrow();
        return slotRepository.findByInstructorAndStartTimeAfter(instructor, LocalDateTime.now());
    }

    @Transactional
    public Booking createBooking(BookingController.CreateBookingRequest req) {
        LessonSlot slot = slotRepository.findById(req.slotId()).orElseThrow();
        if (!slot.isAvailable()) {
            throw new IllegalStateException("Slot already booked");
        }

        User learner;
        if (req.userId() != null) {
            learner = userRepository.findById(req.userId()).orElseThrow();
        } else if (req.email() != null && !req.email().isBlank()) {
            learner = userRepository.findByEmail(req.email()).orElseGet(() -> {
                User u = new User();
                u.setEmail(req.email());
                u.setFullName(req.fullName());
                u.setPhone(req.phone());
                u.setRole(UserRole.LEARNER);
                return userRepository.save(u);
            });
            // update details if provided
            if (req.fullName() != null) learner.setFullName(req.fullName());
            if (req.phone() != null) learner.setPhone(req.phone());
            learner = userRepository.save(learner);
        } else {
            throw new IllegalArgumentException("User information required");
        }

        slot.setAvailable(false);
        Booking booking = new Booking();
        booking.setLearner(learner);
        booking.setSlot(slot);
        booking.setStatus(BookingStatus.CONFIRMED); // Keep existing behavior
        if (req.paymentMethod() != null) {
            booking.setPaymentMethod(req.paymentMethod().toUpperCase());
        }
        booking.setCardLast4(req.cardLast4());
        slotRepository.save(slot);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking createPendingBooking(BookingController.CreateBookingRequest req) {
        LessonSlot slot = slotRepository.findById(req.slotId()).orElseThrow();
        if (!slot.isAvailable()) {
            throw new IllegalStateException("Slot already booked");
        }

        User learner;
        if (req.userId() != null) {
            learner = userRepository.findById(req.userId()).orElseThrow();
        } else if (req.email() != null && !req.email().isBlank()) {
            learner = userRepository.findByEmail(req.email()).orElseGet(() -> {
                User u = new User();
                u.setEmail(req.email());
                u.setFullName(req.fullName());
                u.setPhone(req.phone());
                u.setRole(UserRole.LEARNER);
                return userRepository.save(u);
            });
            if (req.fullName() != null) learner.setFullName(req.fullName());
            if (req.phone() != null) learner.setPhone(req.phone());
            learner = userRepository.save(learner);
        } else {
            throw new IllegalArgumentException("User information required");
        }

        slot.setAvailable(false);
        Booking booking = new Booking();
        booking.setLearner(learner);
        booking.setSlot(slot);
        booking.setStatus(BookingStatus.PENDING); // PENDING for driver confirmation
        if (req.paymentMethod() != null) {
            booking.setPaymentMethod(req.paymentMethod().toUpperCase());
        }
        booking.setCardLast4(req.cardLast4());
        slotRepository.save(slot);
        return bookingRepository.save(booking);
    }

    public List<Booking> bookingsForUser(Long userId) {
        User learner = userRepository.findById(userId).orElseThrow();
        return bookingRepository.findByLearner(learner);
    }

    public List<Booking> allBookings() {
        return bookingRepository.findAll();
    }

    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        booking.setStatus(BookingStatus.CANCELLED);
        LessonSlot slot = booking.getSlot();
        if (slot != null) {
            slot.setAvailable(true);
            slotRepository.save(slot);
        }
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking createBookingFromDriverAvailability(BookingController.CreateDriverAvailabilityBookingRequest req) {
        if (req.instructorId() == null) {
            throw new IllegalArgumentException("Instructor ID is required");
        }
        if (req.date() == null || req.timeSlot() == null) {
            throw new IllegalArgumentException("Date and time slot are required");
        }
        
        Instructor instructor = instructorRepository.findById(req.instructorId())
                .orElseThrow(() -> new IllegalArgumentException("Instructor not found with ID: " + req.instructorId()));
        
        LocalDate date;
        LocalTime time;
        try {
            date = LocalDate.parse(req.date());
            time = LocalTime.parse(req.timeSlot());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date or time format: " + e.getMessage());
        }
        LocalDateTime startTime = LocalDateTime.of(date, time);

        // Check driver availability first
        List<DriverAvailability> availabilities = driverAvailabilityRepository
                .findByInstructorAndDayAndTimeSlot(instructor, date, req.timeSlot());
        
        if (availabilities.isEmpty()) {
            // Try to find any availability for this instructor to help debug
            List<DriverAvailability> allAvail = driverAvailabilityRepository.findByInstructor(instructor);
            throw new IllegalStateException(
                String.format("No availability found for instructor %d on date %s at time %s. " +
                    "Total availabilities for this instructor: %d. " +
                    "Please ensure the admin has enabled this month and the date is available.",
                    req.instructorId(), req.date(), req.timeSlot(), allAvail.size())
            );
        }
        
        // Check if any availability is actually available
        boolean hasAvailable = availabilities.stream()
                .anyMatch(av -> "available".equals(av.getStatus()) && !av.isUnavailableDay());
        
        if (!hasAvailable) {
            throw new IllegalStateException("Time slot is not available");
        }

        // Find or create a LessonSlot
        LessonSlot slot = slotRepository.findByInstructorAndStartTime(instructor, startTime)
                .orElseGet(() -> {
                    LessonSlot newSlot = new LessonSlot();
                    newSlot.setInstructor(instructor);
                    newSlot.setStartTime(startTime);
                    // Use school's default values if available
                    try {
                        if (instructor.getSchool() != null) {
                            newSlot.setDurationMinutes(instructor.getSchool().getDefaultLessonMinutes() != null 
                                    ? instructor.getSchool().getDefaultLessonMinutes() : 60);
                            newSlot.setPrice(instructor.getSchool().getPricePerLesson() != null 
                                    ? instructor.getSchool().getPricePerLesson() : BigDecimal.valueOf(350));
                        } else {
                            newSlot.setDurationMinutes(60); // Default duration
                            newSlot.setPrice(BigDecimal.valueOf(350)); // Default price
                        }
                    } catch (Exception e) {
                        // Fallback to defaults if school access fails
                        newSlot.setDurationMinutes(60);
                        newSlot.setPrice(BigDecimal.valueOf(350));
                    }
                    newSlot.setAvailable(true); // Create as available, will be marked unavailable below
                    return slotRepository.save(newSlot);
                });

        if (!slot.isAvailable()) {
            throw new IllegalStateException("Slot already booked");
        }

        // Update driver availability status to booked
        for (DriverAvailability av : availabilities) {
            if ("available".equals(av.getStatus()) && !av.isUnavailableDay()) {
                av.setStatus("booked");
                driverAvailabilityRepository.save(av);
            }
        }

        // Create learner user
        User learner;
        if (req.userId() != null) {
            // Try to find user by ID first
            learner = userRepository.findById(req.userId()).orElse(null);
            if (learner == null) {
                // User ID doesn't exist, fall back to email if provided
                if (req.email() != null && !req.email().isBlank()) {
                    learner = userRepository.findByEmail(req.email()).orElseGet(() -> {
                        User u = new User();
                        u.setEmail(req.email());
                        u.setFullName(req.fullName());
                        u.setPhone(req.phone());
                        u.setRole(UserRole.LEARNER);
                        return userRepository.save(u);
                    });
                    if (req.fullName() != null) learner.setFullName(req.fullName());
                    if (req.phone() != null) learner.setPhone(req.phone());
                    learner = userRepository.save(learner);
                } else {
                    throw new IllegalArgumentException(
                            "User not found with ID: " + req.userId() + ". Please provide email or login again.");
                }
            }
        } else if (req.email() != null && !req.email().isBlank()) {
            learner = userRepository.findByEmail(req.email()).orElseGet(() -> {
                User u = new User();
                u.setEmail(req.email());
                u.setFullName(req.fullName());
                u.setPhone(req.phone());
                u.setRole(UserRole.LEARNER);
                return userRepository.save(u);
            });
            if (req.fullName() != null) learner.setFullName(req.fullName());
            if (req.phone() != null) learner.setPhone(req.phone());
            learner = userRepository.save(learner);
        } else {
            throw new IllegalArgumentException("User information required (userId or email)");
        }

        slot.setAvailable(false);
        Booking booking = new Booking();
        booking.setLearner(learner);
        booking.setSlot(slot);
        booking.setStatus(BookingStatus.PENDING); // PENDING for driver confirmation
        if (req.paymentMethod() != null) {
            booking.setPaymentMethod(req.paymentMethod().toUpperCase());
        }
        booking.setCardLast4(req.cardLast4());
        slotRepository.save(slot);
        return bookingRepository.save(booking);
    }
}

