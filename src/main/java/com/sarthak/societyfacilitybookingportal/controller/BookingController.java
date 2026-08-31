package com.sarthak.societyfacilitybookingportal.controller;

import com.sarthak.societyfacilitybookingportal.entity.Booking;
import com.sarthak.societyfacilitybookingportal.entity.Slot;
import com.sarthak.societyfacilitybookingportal.entity.User;
import com.sarthak.societyfacilitybookingportal.repository.SlotRepository;
import com.sarthak.societyfacilitybookingportal.repository.UserRepository;
import com.sarthak.societyfacilitybookingportal.service.BookingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BookingController {

    private final BookingService bookingService;
    private final SlotRepository slotRepository;
    private final UserRepository userRepository;

    public BookingController(
            BookingService bookingService,
            SlotRepository slotRepository,
            UserRepository userRepository) {

        this.bookingService = bookingService;
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/booking")
    public String bookingPage(
            @RequestParam Long slotId,
            HttpSession session,
            Model model) {

        if (session.getAttribute("userId") == null) {
            return "redirect:/users/login";
        }

        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found"));

        if (!slot.isAvailable()) {
            model.addAttribute("error", "This slot is no longer available.");
        }

        model.addAttribute("slot", slot);

        return "booking";
    }

    @PostMapping("/booking")
    public String submitBooking(
            @RequestParam Long slotId,
            HttpSession session,
            Model model) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/users/login";
        }

        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!slot.isAvailable()) {
            model.addAttribute("error", "This slot is no longer available.");
            model.addAttribute("slot", slot);
            return "booking";
        }

        Booking booking = bookingService.createBooking(user, slot);

        model.addAttribute("booking", booking);

        return "booking-success";
    }

    @GetMapping("/bookings")
    public String viewBookings(
            HttpSession session,
            Model model) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/users/login";
        }

        model.addAttribute(
                "bookings",
                bookingService.getUserBookings(userId)
        );

        return "bookings";
    }

    @PostMapping("/booking/cancel")
    public String cancelBooking(
            @RequestParam Long bookingId,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/users/login";
        }

        bookingService.cancelBooking(bookingId, userId);

        return "redirect:/bookings";
    }
}
