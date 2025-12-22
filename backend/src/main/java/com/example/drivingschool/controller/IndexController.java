package com.example.drivingschool.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    
    @GetMapping(value = {
        "/",
        "/bookings",
        "/admin",
        "/driver-dashboard",
        "/book-driver",
        "/book-driver/**"
    })
    public String index() {
        return "forward:/index.html";
    }
}

