package com.sarthak.societyfacilitybookingportal.repository;

import com.sarthak.societyfacilitybookingportal.entity.Slot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface SlotRepository extends JpaRepository<Slot, Long> {

    List<Slot> findByFacilityFacilityIdAndSlotDateAndAvailableTrue(
            Long facilityId,
            LocalDate slotDate
    );

    List<Slot> findByFacilityFacilityIdOrderBySlotDateAscStartTimeAsc(
            Long facilityId
    );

    boolean existsByFacilityFacilityId(Long facilityId);

    boolean existsByFacilityFacilityIdAndSlotDateAndStartTimeAndEndTime(
            Long facilityId,
            LocalDate slotDate,
            LocalTime startTime,
            LocalTime endTime
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Slot s WHERE s.slotId = :slotId")
    Optional<Slot> findByIdWithLock(@Param("slotId") Long slotId);
}