package com.sarthak.societyfacilitybookingportal.controller;

import com.sarthak.societyfacilitybookingportal.entity.Facility;
import com.sarthak.societyfacilitybookingportal.entity.FacilityStatus;
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

        Facility facility = facilityService.getFacilityById(facilityId);

        if (facility.getStatus() == FacilityStatus.MAINTENANCE) {
            return "redirect:/facilities/" + facilityId;
        }

        model.addAttribute(
                "slots",
                slotService.getAvailableSlots(facilityId, date)
        );

        model.addAttribute(
                "facility",
                facility
        );

        model.addAttribute("selectedDate", date);
        model.addAttribute("facilityId", facilityId);

        return "slots";
    }
}