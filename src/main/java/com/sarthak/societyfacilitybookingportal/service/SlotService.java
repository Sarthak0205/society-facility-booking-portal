package com.sarthak.societyfacilitybookingportal.service;

import com.sarthak.societyfacilitybookingportal.entity.Facility;
import com.sarthak.societyfacilitybookingportal.entity.Slot;
import com.sarthak.societyfacilitybookingportal.repository.FacilityRepository;
import com.sarthak.societyfacilitybookingportal.repository.SlotRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class SlotService {

    private final SlotRepository slotRepository;
    private final FacilityRepository facilityRepository;

    public SlotService(
            SlotRepository slotRepository,
            FacilityRepository facilityRepository) {

        this.slotRepository = slotRepository;
        this.facilityRepository = facilityRepository;
    }

    /*
     * Resident side
     * Returns only available slots for a facility/date.
     */
    public List<Slot> getAvailableSlots(
            Long facilityId,
            LocalDate slotDate) {

        return slotRepository
                .findByFacilityFacilityIdAndSlotDateAndAvailableTrue(
                        facilityId,
                        slotDate
                );
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

        slotRepository.delete(slot);
    }
    public Slot getSlotById(Long slotId) {

        return slotRepository.findById(slotId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Slot not found"));
    }
}