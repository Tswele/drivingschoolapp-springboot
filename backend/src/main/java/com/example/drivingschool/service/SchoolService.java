package com.example.drivingschool.service;

import com.example.drivingschool.model.Instructor;
import com.example.drivingschool.model.School;
import com.example.drivingschool.repository.InstructorRepository;
import com.example.drivingschool.repository.SchoolRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchoolService {
    private final SchoolRepository schoolRepository;
    private final InstructorRepository instructorRepository;

    public SchoolService(SchoolRepository schoolRepository, InstructorRepository instructorRepository) {
        this.schoolRepository = schoolRepository;
        this.instructorRepository = instructorRepository;
    }

    public List<School> search(String city, String name) {
        if (city != null && !city.isBlank()) {
            return schoolRepository.findByCityIgnoreCase(city);
        }
        if (name != null && !name.isBlank()) {
            return schoolRepository.findByNameContainingIgnoreCase(name);
        }
        return schoolRepository.findAll();
    }

    public School get(Long id) {
        return schoolRepository.findById(id).orElseThrow();
    }

    public List<Instructor> instructors(Long schoolId) {
        School school = get(schoolId);
        return instructorRepository.findBySchool(school);
    }
}

