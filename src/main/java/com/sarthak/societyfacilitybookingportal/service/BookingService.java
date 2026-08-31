package com.sarthak.societyfacilitybookingportal.service;

import com.sarthak.societyfacilitybookingportal.entity.Booking;
import com.sarthak.societyfacilitybookingportal.entity.BookingStatus;
import com.sarthak.societyfacilitybookingportal.entity.Slot;
import com.sarthak.societyfacilitybookingportal.entity.User;
import com.sarthak.societyfacilitybookingportal.repository.BookingRepository;
import com.sarthak.societyfacilitybookingportal.repository.SlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SlotRepository slotRepository;

    public BookingService(
            BookingRepository bookingRepository,
            SlotRepository slotRepository) {

        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
    }

    @Transactional
    public Booking createBooking(User user, Slot slot) {

        if (!slot.isAvailable()) {
            throw new IllegalStateException(
                    "This slot is no longer available."
            );
        }

        Booking booking = new Booking();

        booking.setUser(user);
        booking.setSlot(slot);
        booking.setBookingDate(slot.getSlotDate());
        booking.setStatus(BookingStatus.PENDING);

        slot.setAvailable(false);
        slotRepository.save(slot);

        return bookingRepository.save(booking);
    }

    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    @Transactional
    public Booking cancelBooking(Long bookingId, Long userId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found"));

        if (!booking.getUser().getId().equals(userId))  {
            throw new IllegalStateException(
                    "You are not authorized to cancel this booking."
            );
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Booking is already cancelled"
            );
        }

        booking.setStatus(BookingStatus.CANCELLED);

        Slot slot = booking.getSlot();
        slot.setAvailable(true);

        slotRepository.save(slot);

        return bookingRepository.save(booking);
    }
}
