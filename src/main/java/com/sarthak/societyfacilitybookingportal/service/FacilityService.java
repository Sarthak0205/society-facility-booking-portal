package com.sarthak.societyfacilitybookingportal.service;

import com.sarthak.societyfacilitybookingportal.entity.Facility;
import com.sarthak.societyfacilitybookingportal.entity.FacilityCategory;
import com.sarthak.societyfacilitybookingportal.entity.FacilityStatus;
import com.sarthak.societyfacilitybookingportal.repository.FacilityRepository;
import com.sarthak.societyfacilitybookingportal.repository.SlotRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final SlotRepository slotRepository;

    public FacilityService(
            FacilityRepository facilityRepository,
            SlotRepository slotRepository) {
        this.facilityRepository = facilityRepository;
        this.slotRepository = slotRepository;
    }

    public List<Facility> getAllFacilities() {
        return facilityRepository.findAll();
    }

    public Facility getFacilityById(Long facilityId) {
        return facilityRepository.findById(facilityId)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found"));
    }

    public Facility createFacility(
            String facilityName,
            String description,
            Integer capacity,
            String area,
            String location,
            FacilityCategory category,
            FacilityStatus status) {

        Facility facility = new Facility();
        facility.setFacilityName(facilityName);
        facility.setDescription(description);
        facility.setCapacity(capacity);
        facility.setArea(area);
        facility.setLocation(location);
        facility.setCategory(category != null ? category : FacilityCategory.RECREATION);
        facility.setStatus(status != null ? status : FacilityStatus.ACTIVE);

        return facilityRepository.save(facility);
    }

    public Facility createFacility(
            String facilityName,
            String description) {

        return createFacility(
                facilityName,
                description,
                null,
                null,
                null,
                FacilityCategory.RECREATION,
                FacilityStatus.ACTIVE
        );
    }

    public Facility updateFacility(
            Long facilityId,
            String facilityName,
            String description,
            Integer capacity,
            String area,
            String location,
            FacilityCategory category,
            FacilityStatus status) {

        Facility facility = getFacilityById(facilityId);

        facility.setFacilityName(facilityName);
        facility.setDescription(description);
        facility.setCapacity(capacity);
        facility.setArea(area);
        facility.setLocation(location);
        facility.setCategory(category != null ? category : FacilityCategory.RECREATION);
        facility.setStatus(status != null ? status : FacilityStatus.ACTIVE);

        return facilityRepository.save(facility);
    }

    public Facility updateFacility(
            Long facilityId,
            String facilityName,
            String description) {

        Facility existing = getFacilityById(facilityId);

        return updateFacility(
                facilityId,
                facilityName,
                description,
                existing.getCapacity(),
                existing.getArea(),
                existing.getLocation(),
                existing.getCategory(),
                existing.getStatus()
        );
    }

    public void deleteFacility(Long facilityId) {

        Facility facility = getFacilityById(facilityId);

        if (slotRepository.existsByFacilityFacilityId(facilityId)) {
            throw new IllegalStateException(
                    "This facility cannot be deleted because it has associated slots."
            );
        }

        facilityRepository.delete(facility);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeExistingFacilityMetadata() {
        List<Facility> facilities = facilityRepository.findAll();
        for (Facility facility : facilities) {
            boolean updated = false;

            if (facility.getCapacity() == null || facility.getStatus() == null || facility.getCategory() == null) {
                String name = facility.getFacilityName() != null ? facility.getFacilityName().trim() : "";

                if (name.equalsIgnoreCase("Community Hall")) {
                    facility.setCapacity(150);
                    facility.setArea("2500 sq. ft.");
                    facility.setLocation("Clubhouse, Ground Floor");
                    facility.setCategory(FacilityCategory.EVENT_SPACE);
                    facility.setStatus(FacilityStatus.ACTIVE);
                    updated = true;
                } else if (name.equalsIgnoreCase("Swimming Pool")) {
                    facility.setCapacity(30);
                    facility.setArea("2500 sq. ft.");
                    facility.setLocation("Clubhouse, Ground Floor");
                    facility.setCategory(FacilityCategory.RECREATION);
                    facility.setStatus(FacilityStatus.ACTIVE);
                    updated = true;
                } else if (name.equalsIgnoreCase("Gymnasium")) {
                    facility.setCapacity(25);
                    facility.setArea("1200 sq. ft.");
                    facility.setLocation("Clubhouse, Ground Floor");
                    facility.setCategory(FacilityCategory.FITNESS);
                    facility.setStatus(FacilityStatus.ACTIVE);
                    updated = true;
                } else if (name.equalsIgnoreCase("Badminton Court")) {
                    facility.setCapacity(4);
                    facility.setArea("880 sq. ft.");
                    facility.setLocation("Clubhouse, First Floor");
                    facility.setCategory(FacilityCategory.SPORTS);
                    facility.setStatus(FacilityStatus.ACTIVE);
                    updated = true;
                } else if (name.equalsIgnoreCase("Children's Play Area")) {
                    facility.setCapacity(20);
                    facility.setArea("1500 sq. ft.");
                    facility.setLocation("Clubhouse, Ground Floor");
                    facility.setCategory(FacilityCategory.RECREATION);
                    facility.setStatus(FacilityStatus.ACTIVE);
                    updated = true;
                } else {
                    if (facility.getCapacity() == null) facility.setCapacity(20);
                    if (facility.getArea() == null) facility.setArea("1000 sq. ft.");
                    if (facility.getLocation() == null) facility.setLocation("Clubhouse");
                    if (facility.getCategory() == null) facility.setCategory(FacilityCategory.RECREATION);
                    if (facility.getStatus() == null) facility.setStatus(FacilityStatus.ACTIVE);
                    updated = true;
                }
            }

            if (updated) {
                facilityRepository.save(facility);
            }
        }
    }
}