package com.sarthak.societyfacilitybookingportal.controller;

import com.sarthak.societyfacilitybookingportal.entity.Facility;
import com.sarthak.societyfacilitybookingportal.entity.Slot;
import com.sarthak.societyfacilitybookingportal.entity.UserRole;
import com.sarthak.societyfacilitybookingportal.service.FacilityService;
import com.sarthak.societyfacilitybookingportal.service.SlotService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Controller
@RequestMapping("/admin/slots")
public class AdminSlotController {

    private final SlotService slotService;
    private final FacilityService facilityService;

    public AdminSlotController(
            SlotService slotService,
            FacilityService facilityService) {

        this.slotService = slotService;
        this.facilityService = facilityService;
    }

    private boolean isAdmin(HttpSession session) {

        return session.getAttribute("userId") != null
                && session.getAttribute("userRole") == UserRole.ADMIN;
    }

    /*
     * Admin slot management page.
     *
     * URL:
     * /admin/slots?facilityId=1
     */
    @GetMapping
    public String slots(
            @RequestParam Long facilityId,
            HttpSession session,
            Model model) {

        if (!isAdmin(session)) {
            return "redirect:/facilities";
        }

        Facility facility = facilityService.getFacilityById(facilityId);

        model.addAttribute(
                "facility",
                facility
        );

        model.addAttribute(
                "slots",
                slotService.getSlotsByFacility(facilityId)
        );

        model.addAttribute(
                "facilities",
                facilityService.getAllFacilities()
        );

        model.addAttribute(
                "userName",
                session.getAttribute("userName")
        );

        return "admin/slots";
    }

    /*
     * Show create-slot form.
     */
    @GetMapping("/new")
    public String newSlot(
            @RequestParam Long facilityId,
            HttpSession session,
            Model model) {

        if (!isAdmin(session)) {
            return "redirect:/facilities";
        }

        Facility facility = facilityService.getFacilityById(facilityId);

        Slot slot = new Slot();
        slot.setFacility(facility);
        slot.setSlotDate(LocalDate.now());
        slot.setAvailable(true);

        model.addAttribute("slot", slot);
        model.addAttribute(
                "facilities",
                facilityService.getAllFacilities()
        );
        model.addAttribute("formTitle", "Add Slot");
        model.addAttribute(
                "formAction",
                "/admin/slots"
        );
        model.addAttribute(
                "selectedFacilityId",
                facilityId
        );

        return "admin/slot-form";
    }

    /*
     * Create slot.
     */
    @PostMapping
    public String createSlot(
            @RequestParam Long facilityId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate slotDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime startTime,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime endTime,

            @RequestParam(defaultValue = "false")
            boolean available,

            HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/facilities";
        }

        slotService.createSlot(
                facilityId,
                slotDate,
                startTime,
                endTime,
                available
        );

        return "redirect:/admin/slots?facilityId=" + facilityId;
    }

    /*
     * Show edit-slot form.
     */
    @GetMapping("/edit/{id}")
    public String editSlot(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        if (!isAdmin(session)) {
            return "redirect:/facilities";
        }

        Slot slot = slotService.getSlotById(id);

        model.addAttribute(
                "slot",
                slot
        );

        model.addAttribute(
                "facilities",
                facilityService.getAllFacilities()
        );

        model.addAttribute(
                "formTitle",
                "Edit Slot"
        );

        model.addAttribute(
                "formAction",
                "/admin/slots/" + id
        );

        model.addAttribute(
                "selectedFacilityId",
                slot.getFacility().getFacilityId()
        );

        return "admin/slot-form";
    }

    /*
     * Update slot.
     */
    @PostMapping("/{id}")
    public String updateSlot(
            @PathVariable Long id,

            @RequestParam Long facilityId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate slotDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime startTime,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime endTime,

            @RequestParam(defaultValue = "false")
            boolean available,

            HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/facilities";
        }

        slotService.updateSlot(
                id,
                facilityId,
                slotDate,
                startTime,
                endTime,
                available
        );

        return "redirect:/admin/slots?facilityId=" + facilityId;
    }

    /*
     * Delete slot.
     */
    @PostMapping("/delete/{id}")
    public String deleteSlot(
            @PathVariable Long id,
            @RequestParam Long facilityId,
            HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/facilities";
        }

        slotService.deleteSlot(id);

        return "redirect:/admin/slots?facilityId=" + facilityId;
    }
}