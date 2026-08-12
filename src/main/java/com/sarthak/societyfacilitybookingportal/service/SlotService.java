package com.sarthak.societyfacilitybookingportal.service;

import com.sarthak.societyfacilitybookingportal.entity.Slot;
import com.sarthak.societyfacilitybookingportal.repository.SlotRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SlotService {

    private final SlotRepository slotRepository;

    public SlotService(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    public List<Slot> getAvailableSlots(Long facilityId, LocalDate slotDate) {
        return slotRepository
                .findByFacilityFacilityIdAndSlotDateAndAvailableTrue(
                        facilityId,
                        slotDate
                );
    }
}