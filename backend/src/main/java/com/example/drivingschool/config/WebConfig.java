package com.example.drivingschool.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // Frontend is deployed separately on Netlify
    // No need to serve static files from Spring Boot
    // This config is kept for potential future use if serving frontend from Spring Boot
}
