package com.example.drivingschool.controller;

import com.example.drivingschool.model.*;
import com.example.drivingschool.repository.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    private final SchoolRepository schoolRepository;
    private final InstructorRepository instructorRepository;
    private final LessonSlotRepository slotRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final DriverAvailabilityRepository driverAvailabilityRepository;

    public AdminController(SchoolRepository schoolRepository,
                           InstructorRepository instructorRepository,
                           LessonSlotRepository slotRepository,
                           BookingRepository bookingRepository,
                           ReviewRepository reviewRepository,
                           DriverAvailabilityRepository driverAvailabilityRepository) {
        this.schoolRepository = schoolRepository;
        this.instructorRepository = instructorRepository;
        this.slotRepository = slotRepository;
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
        this.driverAvailabilityRepository = driverAvailabilityRepository;
    }

    // Schools
    public record SchoolRequest(@NotBlank String name,
                                String description,
                                String city,
                                String address,
                                String contactPhone,
                                Double rating,
                                BigDecimal pricePerLesson,
                                Integer defaultLessonMinutes) {}

    @PostMapping("/schools")
    public School createSchool(@RequestBody SchoolRequest req) {
        School s = new School();
        s.setName(req.name());
        s.setDescription(req.description());
        s.setCity(req.city());
        s.setAddress(req.address());
        s.setContactPhone(req.contactPhone());
        s.setRating(req.rating());
        s.setPricePerLesson(req.pricePerLesson());
        s.setDefaultLessonMinutes(req.defaultLessonMinutes());
        return schoolRepository.save(s);
    }

    @PutMapping("/schools/{id}")
    public School updateSchool(@PathVariable("id") Long id, @RequestBody SchoolRequest req) {
        School s = schoolRepository.findById(id).orElseThrow();
        s.setName(req.name());
        s.setDescription(req.description());
        s.setCity(req.city());
        s.setAddress(req.address());
        s.setContactPhone(req.contactPhone());
        s.setRating(req.rating());
        s.setPricePerLesson(req.pricePerLesson());
        s.setDefaultLessonMinutes(req.defaultLessonMinutes());
        return schoolRepository.save(s);
    }

    @DeleteMapping("/schools/{id}")
    public void deleteSchool(@PathVariable("id") Long id) {
        School school = schoolRepository.findById(id).orElseThrow();
        // delete bookings -> slots -> instructors -> school
        List<Instructor> instructors = instructorRepository.findBySchool(school);
        for (Instructor inst : instructors) {
            List<LessonSlot> slots = slotRepository.findByInstructor(inst);
            for (LessonSlot slot : slots) {
                bookingRepository.deleteBySlot(slot);
            }
            slotRepository.deleteAll(slots);
        }
        instructorRepository.deleteAll(instructors);
        schoolRepository.delete(school);
    }

    // Instructors
    public record InstructorRequest(@NotBlank String name,
                                    String bio,
                                    Double rating,
                                    @NotNull Long schoolId) {}

    @PostMapping("/instructors")
    public Instructor createInstructor(@RequestBody InstructorRequest req) {
        School school = schoolRepository.findById(req.schoolId()).orElseThrow();
        Instructor inst = new Instructor();
        inst.setName(req.name());
        inst.setBio(req.bio());
        inst.setRating(req.rating());
        inst.setSchool(school);
        return instructorRepository.save(inst);
    }

    @DeleteMapping("/instructors/{id}")
    public void deleteInstructor(@PathVariable("id") Long id) {
        Instructor inst = instructorRepository.findById(id).orElseThrow();
        List<LessonSlot> slots = slotRepository.findByInstructor(inst);
        for (LessonSlot slot : slots) {
            bookingRepository.deleteBySlot(slot);
        }
        slotRepository.deleteAll(slots);
        instructorRepository.delete(inst);
    }

    // Lesson slots
    public record SlotRequest(@NotNull Long instructorId,
                              @NotNull LocalDateTime startTime,
                              @NotNull Integer durationMinutes,
                              @NotNull BigDecimal price) {}

    @PostMapping("/slots")
    public LessonSlot createSlot(@RequestBody SlotRequest req) {
        Instructor instructor = instructorRepository.findById(req.instructorId()).orElseThrow();
        LessonSlot slot = new LessonSlot();
        slot.setInstructor(instructor);
        slot.setStartTime(req.startTime());
        slot.setDurationMinutes(req.durationMinutes());
        slot.setPrice(req.price());
        slot.setAvailable(true);
        return slotRepository.save(slot);
    }

    @DeleteMapping("/slots/{id}")
    public void deleteSlot(@PathVariable("id") Long id) {
        slotRepository.deleteById(id);
    }

    // Bookings and reviews for admin visibility
    @GetMapping("/bookings")
    public List<Booking> bookings() {
        return bookingRepository.findAll();
    }

    @GetMapping("/reviews")
    public List<Review> reviews() {
        return reviewRepository.findAll();
    }

    // Driver Availability Management
    public record EnableMonthRequest(@NotBlank String month) {}

    @PostMapping("/drivers/{instructorId}/enable-month")
    public java.util.Map<String, String> enableMonth(@PathVariable("instructorId") Long instructorId, @RequestBody EnableMonthRequest req) {
        try {
            Instructor instructor = instructorRepository.findById(instructorId)
                    .orElseThrow(() -> new RuntimeException("Instructor not found"));
            
            // Check if month is already enabled
            List<DriverAvailability> existing = driverAvailabilityRepository.findByInstructorAndMonth(instructor, req.month());
            if (!existing.isEmpty()) {
                return java.util.Map.of("message", "Month already enabled");
            }
            
            // Generate all days and time slots for the month
            String[] timeSlots = {"08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00"};
            java.time.YearMonth yearMonth = java.time.YearMonth.parse(req.month());
            int daysInMonth = yearMonth.lengthOfMonth();

            List<DriverAvailability> availabilities = new java.util.ArrayList<>();
            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = yearMonth.atDay(day);
                // Skip past dates
                if (date.isBefore(LocalDate.now())) {
                    continue;
                }
                for (String timeSlot : timeSlots) {
                    DriverAvailability availability = new DriverAvailability();
                    availability.setInstructor(instructor);
                    availability.setMonth(req.month());
                    availability.setDay(date);
                    availability.setTimeSlot(timeSlot);
                    availability.setStatus("available");
                    availability.setUnavailableDay(false);
                    availabilities.add(availability);
                }
            }
            driverAvailabilityRepository.saveAll(availabilities);
            return java.util.Map.of("message", "Month enabled successfully");
        } catch (Exception e) {
            throw new RuntimeException("Failed to enable month: " + e.getMessage(), e);
        }
    }

    @DeleteMapping("/drivers/{instructorId}/disable-month/{month}")
    public java.util.Map<String, String> disableMonth(@PathVariable("instructorId") Long instructorId, @PathVariable("month") String month) {
        try {
            Instructor instructor = instructorRepository.findById(instructorId)
                    .orElseThrow(() -> new RuntimeException("Instructor not found"));
            List<DriverAvailability> availabilities = driverAvailabilityRepository.findByInstructorAndMonth(instructor, month);
            driverAvailabilityRepository.deleteAll(availabilities);
            return java.util.Map.of("message", "Month disabled successfully");
        } catch (Exception e) {
            throw new RuntimeException("Failed to disable month: " + e.getMessage(), e);
        }
    }

    public record SetUnavailableDayRequest(@NotNull Long instructorId, @NotNull String date) {}

    @PostMapping("/drivers/set-unavailable-day")
    public java.util.Map<String, String> setUnavailableDay(@RequestBody SetUnavailableDayRequest req) {
        try {
            Instructor instructor = instructorRepository.findById(req.instructorId())
                    .orElseThrow(() -> new RuntimeException("Instructor not found"));
            LocalDate date = LocalDate.parse(req.date());
            List<DriverAvailability> existing = driverAvailabilityRepository.findByInstructorAndDay(instructor, date);
            driverAvailabilityRepository.deleteAll(existing);

            DriverAvailability unavailable = new DriverAvailability();
            unavailable.setInstructor(instructor);
            unavailable.setMonth(date.toString().substring(0, 7));
            unavailable.setDay(date);
            unavailable.setTimeSlot("00:00");
            unavailable.setStatus("unavailable");
            unavailable.setUnavailableDay(true);
            driverAvailabilityRepository.save(unavailable);
            return java.util.Map.of("message", "Day set as unavailable");
        } catch (Exception e) {
            throw new RuntimeException("Failed to set unavailable day: " + e.getMessage(), e);
        }
    }

    @GetMapping("/drivers/{instructorId}/availability")
    public List<DriverAvailability> getDriverAvailability(@PathVariable("instructorId") Long instructorId) {
        Instructor instructor = instructorRepository.findById(instructorId).orElseThrow();
        return driverAvailabilityRepository.findByInstructor(instructor);
    }

    @GetMapping("/drivers/{instructorId}/months")
    public java.util.Map<String, Object> getDriverMonths(@PathVariable("instructorId") Long instructorId) {
        Instructor instructor = instructorRepository.findById(instructorId).orElseThrow();
        List<DriverAvailability> allAvail = driverAvailabilityRepository.findByInstructor(instructor);
        
        java.util.Set<String> enabledMonths = new java.util.HashSet<>();
        java.util.Set<String> disabledMonths = new java.util.HashSet<>();
        
        // Get all unique months from availability
        for (DriverAvailability av : allAvail) {
            if (av.getMonth() != null) {
                // Check if month has any available slots (not just unavailable days)
                boolean hasAvailableSlots = allAvail.stream()
                    .filter(a -> a.getMonth().equals(av.getMonth()))
                    .anyMatch(a -> "available".equals(a.getStatus()) || "booked".equals(a.getStatus()));
                
                if (hasAvailableSlots) {
                    enabledMonths.add(av.getMonth());
                } else {
                    // Check if it's a disabled month (all slots are unavailable or deleted)
                    boolean allUnavailable = allAvail.stream()
                        .filter(a -> a.getMonth().equals(av.getMonth()))
                        .allMatch(a -> "unavailable".equals(a.getStatus()) || a.isUnavailableDay());
                    if (allUnavailable) {
                        disabledMonths.add(av.getMonth());
                    }
                }
            }
        }
        
        return java.util.Map.of(
            "enabled", new java.util.ArrayList<>(enabledMonths),
            "disabled", new java.util.ArrayList<>(disabledMonths)
        );
    }

    public record SetUnavailableTimeSlotRequest(@NotNull String date, @NotNull String timeSlot) {}

    @PostMapping("/drivers/{instructorId}/set-unavailable-timeslot")
    public java.util.Map<String, String> setUnavailableTimeSlot(
            @PathVariable("instructorId") Long instructorId,
            @RequestBody SetUnavailableTimeSlotRequest req) {
        try {
            Instructor instructor = instructorRepository.findById(instructorId)
                    .orElseThrow(() -> new RuntimeException("Instructor not found"));
            LocalDate date = LocalDate.parse(req.date());
            
            // Find the specific time slot
            List<DriverAvailability> existing = driverAvailabilityRepository
                    .findByInstructorAndDayAndTimeSlot(instructor, date, req.timeSlot());
            
            if (existing.isEmpty()) {
                // Create a new unavailable entry
                DriverAvailability unavailable = new DriverAvailability();
                unavailable.setInstructor(instructor);
                unavailable.setMonth(date.toString().substring(0, 7));
                unavailable.setDay(date);
                unavailable.setTimeSlot(req.timeSlot());
                unavailable.setStatus("unavailable");
                unavailable.setUnavailableDay(false);
                driverAvailabilityRepository.save(unavailable);
            } else {
                // Update existing entry to unavailable
                for (DriverAvailability av : existing) {
                    av.setStatus("unavailable");
                    driverAvailabilityRepository.save(av);
                }
            }
            
            return java.util.Map.of("message", "Time slot set as unavailable");
        } catch (Exception e) {
            throw new RuntimeException("Failed to set unavailable time slot: " + e.getMessage(), e);
        }
    }

    @PostMapping("/drivers/{instructorId}/set-available-timeslot")
    public java.util.Map<String, String> setAvailableTimeSlot(
            @PathVariable("instructorId") Long instructorId,
            @RequestBody SetUnavailableTimeSlotRequest req) {
        try {
            Instructor instructor = instructorRepository.findById(instructorId)
                    .orElseThrow(() -> new RuntimeException("Instructor not found"));
            LocalDate date = LocalDate.parse(req.date());
            
            // Find the specific time slot
            List<DriverAvailability> existing = driverAvailabilityRepository
                    .findByInstructorAndDayAndTimeSlot(instructor, date, req.timeSlot());
            
            if (!existing.isEmpty()) {
                // Update existing entry to available
                for (DriverAvailability av : existing) {
                    av.setStatus("available");
                    av.setUnavailableDay(false);
                    driverAvailabilityRepository.save(av);
                }
            }
            
            return java.util.Map.of("message", "Time slot set as available");
        } catch (Exception e) {
            throw new RuntimeException("Failed to set available time slot: " + e.getMessage(), e);
        }
    }
}

