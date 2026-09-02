package com.sarthak.societyfacilitybookingportal.controller;

import com.sarthak.societyfacilitybookingportal.entity.UserRole;
import com.sarthak.societyfacilitybookingportal.service.FacilityService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/facilities")
public class AdminFacilityController {

    private final FacilityService facilityService;

    public AdminFacilityController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }

    private boolean isAdmin(HttpSession session) {

        return session.getAttribute("userId") != null
                && session.getAttribute("userRole") == UserRole.ADMIN;
    }

    @GetMapping
    public String facilities(
            HttpSession session,
            Model model) {

        if (!isAdmin(session)) {
            return "redirect:/facilities";
        }

        model.addAttribute(
                "facilities",
                facilityService.getAllFacilities()
        );

        model.addAttribute(
                "userName",
                session.getAttribute("userName")
        );

        return "admin/facilities";
    }

    @GetMapping("/new")
    public String newFacility(
            HttpSession session,
            Model model) {

        if (!isAdmin(session)) {
            return "redirect:/facilities";
        }

        model.addAttribute(
                "facility",
                new com.sarthak.societyfacilitybookingportal.entity.Facility()
        );

        model.addAttribute("formTitle", "Add Facility");
        model.addAttribute("formAction", "/admin/facilities");

        model.addAttribute(
                "userName",
                session.getAttribute("userName")
        );

        return "admin/facility-form";
    }

    @PostMapping
    public String createFacility(
            @RequestParam String facilityName,
            @RequestParam String description,
            HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/facilities";
        }

        facilityService.createFacility(
                facilityName,
                description
        );

        return "redirect:/admin/facilities";
    }

    @GetMapping("/edit/{id}")
    public String editFacility(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        if (!isAdmin(session)) {
            return "redirect:/facilities";
        }

        model.addAttribute(
                "facility",
                facilityService.getFacilityById(id)
        );

        model.addAttribute("formTitle", "Edit Facility");

        model.addAttribute(
                "formAction",
                "/admin/facilities/" + id
        );

        model.addAttribute(
                "userName",
                session.getAttribute("userName")
        );

        return "admin/facility-form";
    }

    @PostMapping("/{id}")
    public String updateFacility(
            @PathVariable Long id,
            @RequestParam String facilityName,
            @RequestParam String description,
            HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/facilities";
        }

        facilityService.updateFacility(
                id,
                facilityName,
                description
        );

        return "redirect:/admin/facilities";
    }

    @PostMapping("/delete/{id}")
    public String deleteFacility(
            @PathVariable Long id,
            HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/facilities";
        }

        facilityService.deleteFacility(id);

        return "redirect:/admin/facilities";
    }
}
