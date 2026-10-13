package com.sarthak.societyfacilitybookingportal.service;

import com.sarthak.societyfacilitybookingportal.entity.BookingStatus;
import com.sarthak.societyfacilitybookingportal.entity.Facility;
import com.sarthak.societyfacilitybookingportal.entity.Slot;
import com.sarthak.societyfacilitybookingportal.repository.FacilityRepository;
import com.sarthak.societyfacilitybookingportal.repository.SlotRepository;
import com.sarthak.societyfacilitybookingportal.repository.BookingRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class SlotService {

    private final SlotRepository slotRepository;
    private final FacilityRepository facilityRepository;
    private final BookingRepository bookingRepository;

    public SlotService(
            SlotRepository slotRepository,
            FacilityRepository facilityRepository,
            BookingRepository bookingRepository) {

        this.slotRepository = slotRepository;
        this.facilityRepository = facilityRepository;
        this.bookingRepository = bookingRepository;
    }

    /*
     * Resident side
     * Returns slots for a facility/date.
     * For Gymnasium, calculates dynamic occupancy and spots availability.
     */
    public List<Slot> getAvailableSlots(
            Long facilityId,
            LocalDate slotDate) {

        Facility facility = facilityRepository.findById(facilityId).orElse(null);

        List<Slot> slots = slotRepository
                .findByFacilityFacilityIdAndSlotDateAndAvailableTrue(
                        facilityId,
                        slotDate
                );

        if (facility != null && facility.isGymnasium()) {
            int capacity = facility.getCapacity() != null ? facility.getCapacity() : 25;
            for (Slot slot : slots) {
                int occupied = (int) bookingRepository
                        .countBySlotSlotIdAndBookingDateAndStatus(
                                slot.getSlotId(),
                                slotDate,
                                BookingStatus.CONFIRMED
                        );
                int available = Math.max(0, capacity - occupied);
                slot.setOccupiedCount(occupied);
                slot.setAvailableCount(available);
                slot.setFull(occupied >= capacity);
            }
        }

        return slots;
    }

    /*
     * Admin side
     * Returns every slot belonging to a facility,
     * including unavailable slots.
     */
    public List<Slot> getSlotsByFacility(Long facilityId) {

        return slotRepository
                .findByFacilityFacilityIdOrderBySlotDateAscStartTimeAsc(
                        facilityId
                );
    }

    /*
     * Admin side
     * Create a new slot.
     */
    public Slot createSlot(
            Long facilityId,
            LocalDate slotDate,
            LocalTime startTime,
            LocalTime endTime,
            boolean available) {

        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start and end time are required.");
        }

        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException(
                    "Start time must be before end time."
            );
        }

        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Facility not found"));

        Slot slot = new Slot();

        slot.setFacility(facility);
        slot.setSlotDate(slotDate);
        slot.setStartTime(startTime);
        slot.setEndTime(endTime);
        slot.setAvailable(available);

        return slotRepository.save(slot);
    }

    /*
     * Admin side
     * Update an existing slot.
     */
    public Slot updateSlot(
            Long slotId,
            Long facilityId,
            LocalDate slotDate,
            LocalTime startTime,
            LocalTime endTime,
            boolean available) {

        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start and end time are required.");
        }

        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException(
                    "Start time must be before end time."
            );
        }

        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Slot not found"));

        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Facility not found"));

        slot.setFacility(facility);
        slot.setSlotDate(slotDate);
        slot.setStartTime(startTime);
        slot.setEndTime(endTime);
        slot.setAvailable(available);

        return slotRepository.save(slot);
    }

    /*
     * Admin side
     * Delete an existing slot.
     */
    public void deleteSlot(Long slotId) {

        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Slot not found"));

        if (bookingRepository.existsBySlotSlotId(slotId)) {
            throw new IllegalStateException(
                    "This slot cannot be deleted because it has booking history."
            );
        }

        slotRepository.delete(slot);
    }
    public Slot getSlotById(Long slotId) {

        return slotRepository.findById(slotId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Slot not found"));
    }

    private record FacilitySchedule(
            Long facilityId,
            String facilityName,
            List<LocalTime[]> ranges
    ) {}

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void initializeCurrentAndUpcomingSlots() {
        List<FacilitySchedule> schedules = List.of(
                new FacilitySchedule(6L, "Community Hall", List.of(
                        new LocalTime[]{LocalTime.of(9, 0), LocalTime.of(11, 0)},
                        new LocalTime[]{LocalTime.of(11, 30), LocalTime.of(13, 30)},
                        new LocalTime[]{LocalTime.of(15, 0), LocalTime.of(17, 0)},
                        new LocalTime[]{LocalTime.of(18, 0), LocalTime.of(20, 0)}
                )),
                new FacilitySchedule(7L, "Swimming Pool", List.of(
                        new LocalTime[]{LocalTime.of(6, 0), LocalTime.of(7, 0)},
                        new LocalTime[]{LocalTime.of(7, 0), LocalTime.of(8, 0)},
                        new LocalTime[]{LocalTime.of(8, 0), LocalTime.of(9, 0)},
                        new LocalTime[]{LocalTime.of(17, 0), LocalTime.of(18, 0)},
                        new LocalTime[]{LocalTime.of(18, 0), LocalTime.of(19, 0)},
                        new LocalTime[]{LocalTime.of(19, 0), LocalTime.of(20, 0)}
                )),
                new FacilitySchedule(8L, "Gymnasium", List.of(
                        new LocalTime[]{LocalTime.of(6, 0), LocalTime.of(7, 0)},
                        new LocalTime[]{LocalTime.of(7, 0), LocalTime.of(8, 0)},
                        new LocalTime[]{LocalTime.of(8, 0), LocalTime.of(9, 0)},
                        new LocalTime[]{LocalTime.of(17, 0), LocalTime.of(18, 0)},
                        new LocalTime[]{LocalTime.of(18, 0), LocalTime.of(19, 0)},
                        new LocalTime[]{LocalTime.of(19, 0), LocalTime.of(20, 0)},
                        new LocalTime[]{LocalTime.of(20, 0), LocalTime.of(21, 0)}
                )),
                new FacilitySchedule(9L, "Badminton Court", List.of(
                        new LocalTime[]{LocalTime.of(6, 0), LocalTime.of(7, 0)},
                        new LocalTime[]{LocalTime.of(7, 0), LocalTime.of(8, 0)},
                        new LocalTime[]{LocalTime.of(8, 0), LocalTime.of(9, 0)},
                        new LocalTime[]{LocalTime.of(16, 0), LocalTime.of(17, 0)},
                        new LocalTime[]{LocalTime.of(17, 0), LocalTime.of(18, 0)},
                        new LocalTime[]{LocalTime.of(18, 0), LocalTime.of(19, 0)},
                        new LocalTime[]{LocalTime.of(19, 0), LocalTime.of(20, 0)},
                        new LocalTime[]{LocalTime.of(20, 0), LocalTime.of(21, 0)}
                )),
                new FacilitySchedule(10L, "Children's Play Area", List.of(
                        new LocalTime[]{LocalTime.of(8, 0), LocalTime.of(9, 0)},
                        new LocalTime[]{LocalTime.of(9, 0), LocalTime.of(10, 0)},
                        new LocalTime[]{LocalTime.of(10, 0), LocalTime.of(11, 0)},
                        new LocalTime[]{LocalTime.of(16, 0), LocalTime.of(17, 0)},
                        new LocalTime[]{LocalTime.of(17, 0), LocalTime.of(18, 0)},
                        new LocalTime[]{LocalTime.of(18, 0), LocalTime.of(19, 0)},
                        new LocalTime[]{LocalTime.of(19, 0), LocalTime.of(20, 0)}
                ))
        );

        LocalDate today = LocalDate.now();

        for (FacilitySchedule schedule : schedules) {
            Facility facility = facilityRepository.findById(schedule.facilityId())
                    .orElseGet(() -> facilityRepository.findAll().stream()
                            .filter(f -> f.getFacilityName() != null
                                    && f.getFacilityName().trim().equalsIgnoreCase(schedule.facilityName()))
                            .findFirst().orElse(null));

            if (facility == null) {
                continue;
            }

            for (int dayOffset = 0; dayOffset <= 7; dayOffset++) {
                LocalDate targetDate = today.plusDays(dayOffset);

                for (LocalTime[] range : schedule.ranges()) {
                    LocalTime startTime = range[0];
                    LocalTime endTime = range[1];

                    boolean exists = slotRepository
                            .existsByFacilityFacilityIdAndSlotDateAndStartTimeAndEndTime(
                                    facility.getFacilityId(),
                                    targetDate,
                                    startTime,
                                    endTime
                            );

                    if (!exists) {
                        Slot slot = new Slot();
                        slot.setFacility(facility);
                        slot.setSlotDate(targetDate);
                        slot.setStartTime(startTime);
                        slot.setEndTime(endTime);
                        slot.setAvailable(true);
                        slotRepository.save(slot);
                    }
                }
            }
        }
    }
}