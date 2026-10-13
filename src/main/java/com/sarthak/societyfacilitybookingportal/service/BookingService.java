package com.sarthak.societyfacilitybookingportal.service;

import com.sarthak.societyfacilitybookingportal.entity.Booking;
import com.sarthak.societyfacilitybookingportal.entity.BookingStatus;
import com.sarthak.societyfacilitybookingportal.entity.Facility;
import com.sarthak.societyfacilitybookingportal.entity.FacilityStatus;
import com.sarthak.societyfacilitybookingportal.entity.Slot;
import com.sarthak.societyfacilitybookingportal.entity.User;
import com.sarthak.societyfacilitybookingportal.repository.BookingRepository;
import com.sarthak.societyfacilitybookingportal.repository.SlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    public int getConfirmedBookingCount(Long slotId, LocalDate bookingDate) {
        return (int) bookingRepository.countBySlotSlotIdAndBookingDateAndStatus(
                slotId,
                bookingDate,
                BookingStatus.CONFIRMED
        );
    }

    public int getAvailableCapacity(Slot slot, LocalDate bookingDate) {
        Facility facility = slot.getFacility();
        int capacity = (facility != null && facility.getCapacity() != null) ? facility.getCapacity() : 25;
        int occupied = getConfirmedBookingCount(slot.getSlotId(), bookingDate);
        return Math.max(0, capacity - occupied);
    }

    public boolean isGymnasiumSlotFull(Slot slot, LocalDate bookingDate) {
        Facility facility = slot.getFacility();
        int capacity = (facility != null && facility.getCapacity() != null) ? facility.getCapacity() : 25;
        int occupied = getConfirmedBookingCount(slot.getSlotId(), bookingDate);
        return occupied >= capacity;
    }

    @Transactional
    public Booking createBooking(User user, Slot slot) {

        if (slot.getFacility() != null && slot.getFacility().getStatus() == FacilityStatus.MAINTENANCE) {
            throw new IllegalStateException(
                    "This facility is currently under maintenance and cannot be booked."
            );
        }

        Facility facility = slot.getFacility();
        boolean isGym = facility != null && facility.isGymnasium();

        if (isGym) {
            slotRepository.findByIdWithLock(slot.getSlotId());
            int capacity = facility.getCapacity() != null ? facility.getCapacity() : 25;
            long confirmedCount = bookingRepository.countBySlotSlotIdAndBookingDateAndStatus(
                    slot.getSlotId(),
                    slot.getSlotDate(),
                    BookingStatus.CONFIRMED
            );
            if (confirmedCount >= capacity) {
                throw new IllegalStateException("This Gymnasium slot is full.");
            }
        } else {
            if (!slot.isAvailable()) {
                throw new IllegalStateException(
                        "This slot is no longer available."
                );
            }
        }

        Booking booking = new Booking();

        booking.setUser(user);
        booking.setSlot(slot);
        booking.setBookingDate(slot.getSlotDate());
        booking.setStatus(BookingStatus.PENDING);

        if (!isGym) {
            slot.setAvailable(false);
            slotRepository.save(slot);
        }

        return bookingRepository.save(booking);
    }

    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAllByOrderByBookingIdDesc();
    }

    public List<Booking> getPendingBookings() {
        return bookingRepository.findByStatusOrderByBookingIdDesc(
                BookingStatus.PENDING
        );
    }

    @Transactional
    public Booking confirmBooking(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending bookings can be confirmed."
            );
        }

        Slot slot = booking.getSlot();
        if (slot != null && slot.getFacility() != null && slot.getFacility().isGymnasium()) {
            slotRepository.findByIdWithLock(slot.getSlotId());
            Facility facility = slot.getFacility();
            int capacity = facility.getCapacity() != null ? facility.getCapacity() : 25;
            long confirmedCount = bookingRepository.countBySlotSlotIdAndBookingDateAndStatus(
                    slot.getSlotId(),
                    booking.getBookingDate(),
                    BookingStatus.CONFIRMED
            );
            if (confirmedCount >= capacity) {
                throw new IllegalStateException("This Gymnasium slot is full.");
            }
        }

        booking.setStatus(BookingStatus.CONFIRMED);

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking rejectBooking(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending bookings can be rejected."
            );
        }

        booking.setStatus(BookingStatus.REJECTED);

        Slot slot = booking.getSlot();
        if (slot != null && (slot.getFacility() == null || !slot.getFacility().isGymnasium())) {
            slot.setAvailable(true);
            slotRepository.save(slot);
        }

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking cancelBooking(Long bookingId, Long userId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
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
        if (slot != null && (slot.getFacility() == null || !slot.getFacility().isGymnasium())) {
            slot.setAvailable(true);
            slotRepository.save(slot);
        }

        return bookingRepository.save(booking);
    }
}