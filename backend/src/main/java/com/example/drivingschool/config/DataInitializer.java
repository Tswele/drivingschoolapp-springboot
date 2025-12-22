package com.example.drivingschool.config;

import com.example.drivingschool.model.*;
import com.example.drivingschool.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(SchoolRepository schoolRepository,
                               InstructorRepository instructorRepository,
                               LessonSlotRepository slotRepository,
                               UserRepository userRepository,
                               ReviewRepository reviewRepository) {
        return args -> {
            if (schoolRepository.count() > 0) {
                return;
            }

            School urban = new School();
            urban.setName("Urban Drive Academy");
            urban.setCity("Johannesburg");
            urban.setAddress("123 Main Rd, Sandton");
            urban.setDescription("Modern fleet, dual controls, nervous drivers welcome.");
            urban.setContactPhone("+27 11 555 0101");
            urban.setPricePerLesson(BigDecimal.valueOf(350));
            urban.setDefaultLessonMinutes(60);
            urban.setRating(4.7);

            School coastal = new School();
            coastal.setName("Coastal Driving School");
            coastal.setCity("Cape Town");
            coastal.setAddress("45 Beach Ave, Sea Point");
            coastal.setDescription("Experienced instructors with automatic/manual options.");
            coastal.setContactPhone("+27 21 555 0202");
            coastal.setPricePerLesson(BigDecimal.valueOf(320));
            coastal.setDefaultLessonMinutes(60);
            coastal.setRating(4.5);
            School midlands = new School();
            midlands.setName("Midlands Driving Institute");
            midlands.setCity("Durban");
            midlands.setAddress("88 Ridge Rd, Musgrave");
            midlands.setDescription("Friendly instructors, highway and city driving specialists.");
            midlands.setContactPhone("+27 31 555 0303");
            midlands.setPricePerLesson(BigDecimal.valueOf(300));
            midlands.setDefaultLessonMinutes(60);
            midlands.setRating(4.4);

            School platinum = new School();
            platinum.setName("Platinum Drive School");
            platinum.setCity("Pretoria");
            platinum.setAddress("12 Paul Kruger St, Pretoria CBD");
            platinum.setDescription("Luxury vehicles, premium driving experience and K53 experts.");
            platinum.setContactPhone("+27 12 555 0404");
            platinum.setPricePerLesson(BigDecimal.valueOf(380));
            platinum.setDefaultLessonMinutes(60);
            platinum.setRating(4.9);

            schoolRepository.saveAll(List.of(urban, coastal, platinum,midlands));

            Instructor sipho = new Instructor();
            sipho.setName("Sipho Dlamini");
            sipho.setBio("10 years experience, patient with first-time learners.");
            sipho.setRating(4.8);
            sipho.setSchool(urban);

            Instructor laila = new Instructor();
            laila.setName("Laila Khan");
            laila.setBio("Defensive driving specialist, great with test prep.");
            laila.setRating(4.6);
            laila.setSchool(coastal);

            Instructor thabo = new Instructor();
            thabo.setName("Thabo Mokoena");
            thabo.setBio("Expert in night driving and highway confidence building.");
            thabo.setRating(4.7);
            thabo.setSchool(midlands);

            Instructor amelia = new Instructor();
            amelia.setName("Amelia Van Rensburg");
            amelia.setBio("Calm, structured lessons with excellent K53 pass rate.");
            amelia.setRating(4.9);
            amelia.setSchool(platinum);

            instructorRepository.saveAll(List.of(sipho, laila,thabo,amelia));

            LessonSlot slot1 = new LessonSlot();
            slot1.setInstructor(sipho);
            slot1.setStartTime(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0));
            slot1.setDurationMinutes(60);
            slot1.setPrice(BigDecimal.valueOf(350));

            LessonSlot slot2 = new LessonSlot();
            slot2.setInstructor(laila);
            slot2.setStartTime(LocalDateTime.now().plusDays(2).withHour(14).withMinute(0));
            slot2.setDurationMinutes(60);
            slot2.setPrice(BigDecimal.valueOf(320));
            LessonSlot slot3 = new LessonSlot();
            slot3.setInstructor(thabo);
            slot3.setStartTime(LocalDateTime.now().plusDays(3).withHour(11).withMinute(0));
            slot3.setDurationMinutes(60);
            slot3.setPrice(BigDecimal.valueOf(300)); // Midlands price

            LessonSlot slot4 = new LessonSlot();
            slot4.setInstructor(amelia);
            slot4.setStartTime(LocalDateTime.now().plusDays(4).withHour(10).withMinute(0));
            slot4.setDurationMinutes(60);
            slot4.setPrice(BigDecimal.valueOf(380)); // Platinum price

            slotRepository.saveAll(List.of(slot1, slot2, slot3, slot4));

            User learner = new User();
            learner.setFullName("Test Learner");
            learner.setEmail("learner@example.com");
            learner.setPhone("0820000000");
            learner.setRole(UserRole.LEARNER);
            learner.setPassword("password");
            userRepository.save(learner);

            User admin = new User();
            admin.setFullName("Admin User");
            admin.setEmail("admin@example.com");
            admin.setPhone("0820000001");
            admin.setRole(UserRole.ADMIN);
            admin.setPassword("admin");
            userRepository.save(admin);

            Review review = new Review();
            review.setReviewer(learner);
            review.setSchool(urban);
            review.setRating(5);
            review.setComment("Friendly instructor and clean car!");
            reviewRepository.save(review);
        };
    }
}

