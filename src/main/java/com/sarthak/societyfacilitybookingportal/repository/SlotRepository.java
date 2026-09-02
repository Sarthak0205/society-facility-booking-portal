package com.sarthak.societyfacilitybookingportal.repository;

import com.sarthak.societyfacilitybookingportal.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SlotRepository extends JpaRepository<Slot, Long> {

    List<Slot> findByFacilityFacilityIdAndSlotDateAndAvailableTrue(
            Long facilityId,
            LocalDate slotDate
    );

    List<Slot> findByFacilityFacilityIdOrderBySlotDateAscStartTimeAsc(
            Long facilityId
    );
}