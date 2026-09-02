package com.sarthak.societyfacilitybookingportal.controller;

import com.sarthak.societyfacilitybookingportal.service.FacilityService;
import com.sarthak.societyfacilitybookingportal.service.SlotService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class SlotController {

    private final SlotService slotService;
    private final FacilityService facilityService;

    public SlotController(
            SlotService slotService,
            FacilityService facilityService) {

        this.slotService = slotService;
        this.facilityService = facilityService;
    }

    @GetMapping("/slots")
    public String viewSlots(
            @RequestParam Long facilityId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            HttpSession session,
            Model model) {

        if (session.getAttribute("userId") == null) {
            return "redirect:/users/login";
        }

        model.addAttribute(
                "slots",
                slotService.getAvailableSlots(facilityId, date)
        );

        model.addAttribute(
                "facility",
                facilityService.getFacilityById(facilityId)
        );

        model.addAttribute("selectedDate", date);
        model.addAttribute("facilityId", facilityId);

        return "slots";
    }
}