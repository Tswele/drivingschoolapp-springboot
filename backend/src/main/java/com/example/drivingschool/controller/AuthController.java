package com.example.drivingschool.controller;

import com.example.drivingschool.model.User;
import com.example.drivingschool.model.UserRole;
import com.example.drivingschool.repository.UserRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public record SignupRequest(
            @NotBlank String fullName,
            @Email String email,
            String phone,
            @NotBlank String password
    ) {}

    public record LoginRequest(
            @Email String email,
            @NotBlank String password
    ) {}

    @PostMapping("/signup")
    public User signup(@RequestBody SignupRequest req) {
        User user = userRepository.findByEmail(req.email()).orElse(new User());
        user.setFullName(req.fullName());
        user.setEmail(req.email());
        user.setPhone(req.phone());
        user.setPassword(req.password()); // demo only, no hashing
        user.setRole(UserRole.LEARNER);
        return userRepository.save(user);
    }

    @PostMapping("/login")
    public User login(@RequestBody LoginRequest req) {
        User user = userRepository.findByEmail(req.email()).orElseThrow();
        if (!user.getPassword().equals(req.password())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return user;
    }
}

