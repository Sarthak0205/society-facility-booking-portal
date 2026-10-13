package com.sarthak.societyfacilitybookingportal;

import com.sarthak.societyfacilitybookingportal.entity.*;
import com.sarthak.societyfacilitybookingportal.repository.BookingRepository;
import com.sarthak.societyfacilitybookingportal.repository.FacilityRepository;
import com.sarthak.societyfacilitybookingportal.repository.SlotRepository;
import com.sarthak.societyfacilitybookingportal.repository.UserRepository;
import com.sarthak.societyfacilitybookingportal.service.BookingService;
import com.sarthak.societyfacilitybookingportal.service.FacilityService;
import com.sarthak.societyfacilitybookingportal.service.SlotService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GymnasiumDynamicCapacityTest {

    @Autowired
    private FacilityService facilityService;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private SlotService slotService;

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    private Facility testGymnasium;
    private Slot testSlot;
    private User testUser;
    private final List<Booking> testBookings = new ArrayList<>();
    private final List<Slot> extraSlots = new ArrayList<>();

    @BeforeEach
    void setUp() {
        testUser = userRepository.findByEmail("test-gym@example.com").orElseGet(() -> {
            User user = new User();
            user.setEmail("test-gym@example.com");
            user.setFullName("Gym Tester");
            user.setPassword("123456");
            user.setRole(UserRole.RESIDENT);
            return userRepository.save(user);
        });

        testGymnasium = facilityService.createFacility(
                "Gymnasium",
                "Dynamic Capacity Gymnasium",
                25,
                "1200 sq. ft.",
                "Clubhouse",
                FacilityCategory.FITNESS,
                FacilityStatus.ACTIVE
        );

        testSlot = slotService.createSlot(
                testGymnasium.getFacilityId(),
                LocalDate.of(2026, 10, 13),
                LocalTime.of(18, 0),
                LocalTime.of(19, 0),
                true
        );
    }

    @AfterEach
    void tearDown() {
        if (!testBookings.isEmpty()) {
            bookingRepository.deleteAll(testBookings);
            testBookings.clear();
        }
        for (Slot s : extraSlots) {
            bookingRepository.deleteAll(bookingRepository.findAll().stream()
                    .filter(b -> b.getSlot().getSlotId().equals(s.getSlotId())).toList());
            slotRepository.delete(s);
        }
        extraSlots.clear();

        if (testSlot != null && slotRepository.existsById(testSlot.getSlotId())) {
            bookingRepository.deleteAll(bookingRepository.findAll().stream()
                    .filter(b -> b.getSlot().getSlotId().equals(testSlot.getSlotId())).toList());
            slotRepository.delete(testSlot);
        }

        if (testGymnasium != null && facilityRepository.existsById(testGymnasium.getFacilityId())) {
            facilityRepository.delete(testGymnasium);
        }
    }

    private Booking createBookingWithStatus(Slot slot, LocalDate date, BookingStatus status) {
        Booking booking = new Booking();
        booking.setUser(testUser);
        booking.setSlot(slot);
        booking.setBookingDate(date);
        booking.setStatus(status);
        Booking saved = bookingRepository.save(booking);
        testBookings.add(saved);
        return saved;
    }

    // A. Capacity calculation: 25 capacity + 0 confirmed = 25 available
    @Test
    void testA_CapacityCalculation_ZeroConfirmed() {
        int available = bookingService.getAvailableCapacity(testSlot, testSlot.getSlotDate());
        int occupied = bookingService.getConfirmedBookingCount(testSlot.getSlotId(), testSlot.getSlotDate());

        assertEquals(25, available);
        assertEquals(0, occupied);

        List<Slot> slots = slotService.getAvailableSlots(testGymnasium.getFacilityId(), testSlot.getSlotDate());
        Slot retrieved = slots.stream().filter(s -> s.getSlotId().equals(testSlot.getSlotId())).findFirst().orElseThrow();
        assertEquals(0, retrieved.getOccupiedCount());
        assertEquals(25, retrieved.getAvailableCount());
        assertFalse(retrieved.isFull());
    }

    // B. Partial occupancy: 25 capacity + 18 confirmed = 7 available
    @Test
    void testB_PartialOccupancy_EighteenConfirmed() {
        for (int i = 0; i < 18; i++) {
            createBookingWithStatus(testSlot, testSlot.getSlotDate(), BookingStatus.CONFIRMED);
        }

        int available = bookingService.getAvailableCapacity(testSlot, testSlot.getSlotDate());
        int occupied = bookingService.getConfirmedBookingCount(testSlot.getSlotId(), testSlot.getSlotDate());

        assertEquals(18, occupied);
        assertEquals(7, available);

        List<Slot> slots = slotService.getAvailableSlots(testGymnasium.getFacilityId(), testSlot.getSlotDate());
        Slot retrieved = slots.stream().filter(s -> s.getSlotId().equals(testSlot.getSlotId())).findFirst().orElseThrow();
        assertEquals(18, retrieved.getOccupiedCount());
        assertEquals(7, retrieved.getAvailableCount());
        assertFalse(retrieved.isFull());
    }

    // C. Full slot: 25 capacity + 25 confirmed = 0 available
    @Test
    void testC_FullSlot_TwentyFiveConfirmed() {
        for (int i = 0; i < 25; i++) {
            createBookingWithStatus(testSlot, testSlot.getSlotDate(), BookingStatus.CONFIRMED);
        }

        int available = bookingService.getAvailableCapacity(testSlot, testSlot.getSlotDate());
        int occupied = bookingService.getConfirmedBookingCount(testSlot.getSlotId(), testSlot.getSlotDate());

        assertEquals(25, occupied);
        assertEquals(0, available);
        assertTrue(bookingService.isGymnasiumSlotFull(testSlot, testSlot.getSlotDate()));

        List<Slot> slots = slotService.getAvailableSlots(testGymnasium.getFacilityId(), testSlot.getSlotDate());
        Slot retrieved = slots.stream().filter(s -> s.getSlotId().equals(testSlot.getSlotId())).findFirst().orElseThrow();
        assertEquals(25, retrieved.getOccupiedCount());
        assertEquals(0, retrieved.getAvailableCount());
        assertTrue(retrieved.isFull());
    }

    // D. Cancelled booking: A CANCELLED booking does not count toward occupancy.
    @Test
    void testD_CancelledBookingDoesNotCountTowardOccupancy() {
        createBookingWithStatus(testSlot, testSlot.getSlotDate(), BookingStatus.CANCELLED);
        createBookingWithStatus(testSlot, testSlot.getSlotDate(), BookingStatus.CONFIRMED);

        int occupied = bookingService.getConfirmedBookingCount(testSlot.getSlotId(), testSlot.getSlotDate());
        int available = bookingService.getAvailableCapacity(testSlot, testSlot.getSlotDate());

        assertEquals(1, occupied);
        assertEquals(24, available);
    }

    // E. Rejected booking: A REJECTED booking does not count toward occupancy.
    @Test
    void testE_RejectedBookingDoesNotCountTowardOccupancy() {
        createBookingWithStatus(testSlot, testSlot.getSlotDate(), BookingStatus.REJECTED);
        createBookingWithStatus(testSlot, testSlot.getSlotDate(), BookingStatus.CONFIRMED);

        int occupied = bookingService.getConfirmedBookingCount(testSlot.getSlotId(), testSlot.getSlotDate());
        int available = bookingService.getAvailableCapacity(testSlot, testSlot.getSlotDate());

        assertEquals(1, occupied);
        assertEquals(24, available);
    }

    // F. Pending booking: A PENDING booking does not count toward occupancy.
    @Test
    void testF_PendingBookingDoesNotCountTowardOccupancy() {
        createBookingWithStatus(testSlot, testSlot.getSlotDate(), BookingStatus.PENDING);
        createBookingWithStatus(testSlot, testSlot.getSlotDate(), BookingStatus.CONFIRMED);

        int occupied = bookingService.getConfirmedBookingCount(testSlot.getSlotId(), testSlot.getSlotDate());
        int available = bookingService.getAvailableCapacity(testSlot, testSlot.getSlotDate());

        assertEquals(1, occupied);
        assertEquals(24, available);
    }

    // G. Booking when space exists: A new Gymnasium booking is allowed when available capacity > 0.
    @Test
    void testG_BookingAllowedWhenSpaceExists() {
        for (int i = 0; i < 18; i++) {
            createBookingWithStatus(testSlot, testSlot.getSlotDate(), BookingStatus.CONFIRMED);
        }

        Booking newBooking = assertDoesNotThrow(() -> bookingService.createBooking(testUser, testSlot));
        testBookings.add(newBooking);

        assertEquals(BookingStatus.PENDING, newBooking.getStatus());
        // Since it is pending, occupancy remains 18
        assertEquals(18, bookingService.getConfirmedBookingCount(testSlot.getSlotId(), testSlot.getSlotDate()));
        assertEquals(7, bookingService.getAvailableCapacity(testSlot, testSlot.getSlotDate()));
    }

    // H. Booking when full: A new Gymnasium booking is rejected when confirmed bookings == capacity.
    @Test
    void testH_BookingRejectedWhenFull() {
        for (int i = 0; i < 25; i++) {
            createBookingWithStatus(testSlot, testSlot.getSlotDate(), BookingStatus.CONFIRMED);
        }

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> bookingService.createBooking(testUser, testSlot)
        );

        assertEquals("This Gymnasium slot is full.", exception.getMessage());
    }

    // I. Date isolation: Bookings on 2026-10-13 must not affect availability on 2026-10-14.
    @Test
    void testI_DateIsolation() {
        LocalDate date1 = LocalDate.of(2026, 10, 13);
        LocalDate date2 = LocalDate.of(2026, 10, 14);

        Slot slotDate2 = slotService.createSlot(
                testGymnasium.getFacilityId(),
                date2,
                LocalTime.of(18, 0),
                LocalTime.of(19, 0),
                true
        );
        extraSlots.add(slotDate2);

        // 20 confirmed bookings on date1
        for (int i = 0; i < 20; i++) {
            createBookingWithStatus(testSlot, date1, BookingStatus.CONFIRMED);
        }

        // 8 confirmed bookings on date2
        for (int i = 0; i < 8; i++) {
            createBookingWithStatus(slotDate2, date2, BookingStatus.CONFIRMED);
        }

        assertEquals(20, bookingService.getConfirmedBookingCount(testSlot.getSlotId(), date1));
        assertEquals(5, bookingService.getAvailableCapacity(testSlot, date1));

        assertEquals(8, bookingService.getConfirmedBookingCount(slotDate2.getSlotId(), date2));
        assertEquals(17, bookingService.getAvailableCapacity(slotDate2, date2));
    }

    // J. Cancellation restores capacity: Cancelling a confirmed Gymnasium booking makes that spot available again.
    @Test
    void testJ_CancellationRestoresCapacity() {
        // 24 confirmed bookings
        for (int i = 0; i < 23; i++) {
            createBookingWithStatus(testSlot, testSlot.getSlotDate(), BookingStatus.CONFIRMED);
        }
        Booking confirmedBooking = createBookingWithStatus(testSlot, testSlot.getSlotDate(), BookingStatus.CONFIRMED);

        assertEquals(24, bookingService.getConfirmedBookingCount(testSlot.getSlotId(), testSlot.getSlotDate()));
        assertEquals(1, bookingService.getAvailableCapacity(testSlot, testSlot.getSlotDate()));

        // Cancel the confirmed booking
        Booking cancelledBooking = bookingService.cancelBooking(confirmedBooking.getBookingId(), testUser.getId());

        assertEquals(BookingStatus.CANCELLED, cancelledBooking.getStatus());
        assertEquals(23, bookingService.getConfirmedBookingCount(testSlot.getSlotId(), testSlot.getSlotDate()));
        assertEquals(2, bookingService.getAvailableCapacity(testSlot, testSlot.getSlotDate()));
    }

    // K. Admin confirmation: Confirming a pending Gymnasium booking consumes one available spot.
    @Test
    void testK_AdminConfirmationConsumesSpot() {
        Booking pendingBooking = createBookingWithStatus(testSlot, testSlot.getSlotDate(), BookingStatus.PENDING);

        assertEquals(0, bookingService.getConfirmedBookingCount(testSlot.getSlotId(), testSlot.getSlotDate()));
        assertEquals(25, bookingService.getAvailableCapacity(testSlot, testSlot.getSlotDate()));

        Booking confirmedBooking = bookingService.confirmBooking(pendingBooking.getBookingId());

        assertEquals(BookingStatus.CONFIRMED, confirmedBooking.getStatus());
        assertEquals(1, bookingService.getConfirmedBookingCount(testSlot.getSlotId(), testSlot.getSlotDate()));
        assertEquals(24, bookingService.getAvailableCapacity(testSlot, testSlot.getSlotDate()));
    }

    // L. Admin confirmation when full: If the slot becomes full before confirmation, prevent confirmation.
    @Test
    void testL_AdminConfirmationWhenFullPrevented() {
        Booking pendingBooking = createBookingWithStatus(testSlot, testSlot.getSlotDate(), BookingStatus.PENDING);

        // Slot becomes full with 25 confirmed bookings
        for (int i = 0; i < 25; i++) {
            createBookingWithStatus(testSlot, testSlot.getSlotDate(), BookingStatus.CONFIRMED);
        }

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> bookingService.confirmBooking(pendingBooking.getBookingId())
        );

        assertEquals("This Gymnasium slot is full.", exception.getMessage());

        // Booking remains PENDING
        Booking reloaded = bookingRepository.findById(pendingBooking.getBookingId()).orElseThrow();
        assertEquals(BookingStatus.PENDING, reloaded.getStatus());
    }
}
