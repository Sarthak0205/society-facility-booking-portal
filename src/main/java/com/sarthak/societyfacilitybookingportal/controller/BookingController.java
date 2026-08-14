package com.sarthak.societyfacilitybookingportal.controller;

import com.sarthak.societyfacilitybookingportal.entity.Booking;
import com.sarthak.societyfacilitybookingportal.entity.Slot;
import com.sarthak.societyfacilitybookingportal.entity.User;
import com.sarthak.societyfacilitybookingportal.repository.BookingRepository;
import com.sarthak.societyfacilitybookingportal.repository.SlotRepository;
import com.sarthak.societyfacilitybookingportal.repository.UserRepository;
import com.sarthak.societyfacilitybookingportal.service.BookingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BookingController {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final SlotRepository slotRepository;
    private final UserRepository userRepository;

    public BookingController(
            BookingService bookingService,
            BookingRepository bookingRepository,
            SlotRepository slotRepository,
            UserRepository userRepository) {

        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/booking")
    public String bookingPage(
            @RequestParam Long slotId,
            Model model) {

        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found"));

        model.addAttribute("slot", slot);

        return "booking";
    }

    @PostMapping("/booking")
    public String submitBooking(
            @RequestParam Long slotId,
            @RequestParam Long userId,
            Model model) {

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

        slot.setAvailable(false);
        slotRepository.save(slot);

        model.addAttribute("booking", booking);

        return "booking-success";
    }

    @GetMapping("/bookings")
    public String viewBookings(
            @RequestParam Long userId,
            Model model) {

        model.addAttribute(
                "bookings",
                bookingService.getUserBookings(userId)
        );

        model.addAttribute("userId", userId);

        return "bookings";
    }

    @PostMapping("/booking/cancel")
    public String cancelBooking(
            @RequestParam Long bookingId,
            @RequestParam Long userId,
            Model model) {

        bookingService.cancelBooking(bookingId);

        return "redirect:/bookings?userId=" + userId;
    }
}