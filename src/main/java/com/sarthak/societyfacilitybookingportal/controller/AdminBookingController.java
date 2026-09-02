package com.sarthak.societyfacilitybookingportal.controller;

import com.sarthak.societyfacilitybookingportal.entity.UserRole;
import com.sarthak.societyfacilitybookingportal.service.BookingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/bookings")
public class AdminBookingController {

    private final BookingService bookingService;

    public AdminBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    private boolean isAdmin(HttpSession session) {

        return session.getAttribute("userId") != null
                && session.getAttribute("userRole") == UserRole.ADMIN;
    }

    @GetMapping
    public String bookings(
            HttpSession session,
            Model model) {

        if (!isAdmin(session)) {
            return "redirect:/facilities";
        }

        model.addAttribute(
                "bookings",
                bookingService.getAllBookings()
        );

        model.addAttribute(
                "userName",
                session.getAttribute("userName")
        );

        return "admin/bookings";
    }

    @PostMapping("/confirm/{id}")
    public String confirmBooking(
            @PathVariable Long id,
            HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/facilities";
        }

        bookingService.confirmBooking(id);

        return "redirect:/admin/bookings";
    }

    @PostMapping("/reject/{id}")
    public String rejectBooking(
            @PathVariable Long id,
            HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/facilities";
        }

        bookingService.rejectBooking(id);

        return "redirect:/admin/bookings";
    }
}