package com.sarthak.societyfacilitybookingportal;

import com.sarthak.societyfacilitybookingportal.entity.*;
import com.sarthak.societyfacilitybookingportal.repository.BookingRepository;
import com.sarthak.societyfacilitybookingportal.repository.FacilityRepository;
import com.sarthak.societyfacilitybookingportal.repository.SlotRepository;
import com.sarthak.societyfacilitybookingportal.repository.UserRepository;
import com.sarthak.societyfacilitybookingportal.service.BookingService;
import com.sarthak.societyfacilitybookingportal.service.FacilityService;
import com.sarthak.societyfacilitybookingportal.service.SlotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FacilitySlotInitializationTest {

    @Autowired
    private SlotService slotService;

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private FacilityService facilityService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    private final List<Long> facilityIds = List.of(6L, 7L, 8L, 9L, 10L);

    @BeforeEach
    void setUp() {
        slotService.initializeCurrentAndUpcomingSlots();
    }

    // A. Every active facility has slots for today.
    @Test
    void testA_EveryActiveFacilityHasSlotsForToday() {
        LocalDate today = LocalDate.now();

        for (Long facilityId : facilityIds) {
            Facility facility = facilityRepository.findById(facilityId).orElse(null);
            if (facility != null && facility.getStatus() == FacilityStatus.ACTIVE) {
                List<Slot> slots = slotRepository
                        .findByFacilityFacilityIdOrderBySlotDateAscStartTimeAsc(facilityId)
                        .stream()
                        .filter(s -> s.getSlotDate().equals(today))
                        .toList();

                assertFalse(slots.isEmpty(), "Facility " + facility.getFacilityName() + " has no slots for today");
            }
        }

        // Verify specific time slots for Community Hall for today
        assertTrue(slotRepository.existsByFacilityFacilityIdAndSlotDateAndStartTimeAndEndTime(
                6L, today, LocalTime.of(9, 0), LocalTime.of(11, 0)
        ));
        assertTrue(slotRepository.existsByFacilityFacilityIdAndSlotDateAndStartTimeAndEndTime(
                6L, today, LocalTime.of(11, 30), LocalTime.of(13, 30)
        ));
        assertTrue(slotRepository.existsByFacilityFacilityIdAndSlotDateAndStartTimeAndEndTime(
                6L, today, LocalTime.of(15, 0), LocalTime.of(17, 0)
        ));
        assertTrue(slotRepository.existsByFacilityFacilityIdAndSlotDateAndStartTimeAndEndTime(
                6L, today, LocalTime.of(18, 0), LocalTime.of(20, 0)
        ));
    }

    // B. Every active facility has slots for tomorrow.
    @Test
    void testB_EveryActiveFacilityHasSlotsForTomorrow() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        for (Long facilityId : facilityIds) {
            Facility facility = facilityRepository.findById(facilityId).orElse(null);
            if (facility != null && facility.getStatus() == FacilityStatus.ACTIVE) {
                List<Slot> slots = slotRepository
                        .findByFacilityFacilityIdOrderBySlotDateAscStartTimeAsc(facilityId)
                        .stream()
                        .filter(s -> s.getSlotDate().equals(tomorrow))
                        .toList();

                assertFalse(slots.isEmpty(), "Facility " + facility.getFacilityName() + " has no slots for tomorrow");
            }
        }
    }

    // C. Every active facility has slots for the upcoming date range (Today through Today + 7 days).
    @Test
    void testC_EveryActiveFacilityHasSlotsForUpcomingDateRange() {
        LocalDate today = LocalDate.now();

        for (int dayOffset = 0; dayOffset <= 7; dayOffset++) {
            LocalDate date = today.plusDays(dayOffset);

            for (Long facilityId : facilityIds) {
                Facility facility = facilityRepository.findById(facilityId).orElse(null);
                if (facility != null && facility.getStatus() == FacilityStatus.ACTIVE) {
                    List<Slot> slots = slotRepository
                            .findByFacilityFacilityIdOrderBySlotDateAscStartTimeAsc(facilityId)
                            .stream()
                            .filter(s -> s.getSlotDate().equals(date))
                            .toList();

                    assertFalse(slots.isEmpty(), "Facility " + facility.getFacilityName() + " has no slots for " + date);
                }
            }
        }
    }

    // D. Running the initializer twice does not create duplicates.
    @Test
    void testD_RunningInitializerTwiceDoesNotCreateDuplicates() {
        long countBefore = slotRepository.count();

        // Run initializer again
        slotService.initializeCurrentAndUpcomingSlots();

        long countAfter = slotRepository.count();

        assertEquals(countBefore, countAfter, "Re-running initializer created duplicate slots!");
    }

    // E. Existing bookings are preserved.
    @Test
    void testE_ExistingBookingsArePreserved() {
        long bookingCountBefore = bookingRepository.count();

        // Re-run slot initializer
        slotService.initializeCurrentAndUpcomingSlots();

        long bookingCountAfter = bookingRepository.count();

        assertEquals(bookingCountBefore, bookingCountAfter, "Slot initialization altered existing booking count");
    }

    // F. Existing slot deletion restrictions continue working.
    @Test
    void testF_SlotDeletionBlockedWhenBookingsExist() {
        User testUser = userRepository.findAll().stream().findFirst().orElseThrow();
        Facility pool = facilityRepository.findById(7L).orElseThrow();

        Slot testSlot = slotService.createSlot(
                pool.getFacilityId(),
                LocalDate.now().plusDays(5),
                LocalTime.of(12, 0),
                LocalTime.of(13, 0),
                true
        );

        Booking booking = new Booking();
        booking.setUser(testUser);
        booking.setSlot(testSlot);
        booking.setBookingDate(testSlot.getSlotDate());
        booking.setStatus(BookingStatus.CONFIRMED);
        Booking savedBooking = bookingRepository.save(booking);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> slotService.deleteSlot(testSlot.getSlotId())
        );

        assertTrue(ex.getMessage().contains("booking history"));

        // Cleanup
        bookingRepository.delete(savedBooking);
        slotRepository.delete(testSlot);
    }

    // G. Gymnasium slots work with the dynamic capacity calculation.
    @Test
    void testG_GymnasiumSlotsWorkWithDynamicCapacity() {
        LocalDate today = LocalDate.now();
        List<Slot> gymSlots = slotService.getAvailableSlots(8L, today);

        assertFalse(gymSlots.isEmpty(), "Gymnasium should have slots for today");

        Slot gymSlot = gymSlots.getFirst();
        assertEquals(25, gymSlot.getFacility().getCapacity());

        int available = bookingService.getAvailableCapacity(gymSlot, today);
        int occupied = bookingService.getConfirmedBookingCount(gymSlot.getSlotId(), today);

        assertEquals(25 - occupied, available);
    }

    // H. Maintenance facilities cannot be booked.
    @Test
    void testH_MaintenanceFacilitiesCannotBeBooked() {
        User testUser = userRepository.findAll().stream().findFirst().orElseThrow();

        Facility maintenanceFacility = facilityService.createFacility(
                "Maintenance Test Facility",
                "Facility currently closed for maintenance.",
                10,
                "500 sq. ft.",
                "Block D",
                FacilityCategory.RECREATION,
                FacilityStatus.MAINTENANCE
        );

        Slot maintenanceSlot = slotService.createSlot(
                maintenanceFacility.getFacilityId(),
                LocalDate.now().plusDays(2),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                true
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> bookingService.createBooking(testUser, maintenanceSlot)
        );

        assertTrue(exception.getMessage().contains("under maintenance"));

        // Cleanup
        slotRepository.delete(maintenanceSlot);
        facilityRepository.delete(maintenanceFacility);
    }
}
