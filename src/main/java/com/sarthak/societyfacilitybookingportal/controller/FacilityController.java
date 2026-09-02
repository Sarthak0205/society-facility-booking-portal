package com.sarthak.societyfacilitybookingportal.controller;

import com.sarthak.societyfacilitybookingportal.service.FacilityService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
public class FacilityController {

    private final FacilityService facilityService;

    public FacilityController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }

    @GetMapping("/facilities")
    public String viewFacilities(
            HttpSession session,
            Model model) {

        // User must be logged in
        if (session.getAttribute("userId") == null) {
            return "redirect:/users/login";
        }

        // Load facilities from the database
        model.addAttribute(
                "facilities",
                facilityService.getAllFacilities()
        );

        // Default date shown on the facilities page
        model.addAttribute(
                "selectedDate",
                LocalDate.now()
        );

        // Pass logged-in user's name to the UI
        model.addAttribute(
                "userName",
                session.getAttribute("userName")
        );

        return "facilities";
    }
}