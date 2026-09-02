package com.sarthak.societyfacilitybookingportal.service;

import com.sarthak.societyfacilitybookingportal.entity.Facility;
import com.sarthak.societyfacilitybookingportal.repository.FacilityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FacilityService {

    private final FacilityRepository facilityRepository;

    public FacilityService(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
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
            String description) {

        Facility facility = new Facility();
        facility.setFacilityName(facilityName);
        facility.setDescription(description);

        return facilityRepository.save(facility);
    }

    public Facility updateFacility(
            Long facilityId,
            String facilityName,
            String description) {

        Facility facility = getFacilityById(facilityId);

        facility.setFacilityName(facilityName);
        facility.setDescription(description);

        return facilityRepository.save(facility);
    }

    public void deleteFacility(Long facilityId) {

        Facility facility = getFacilityById(facilityId);

        facilityRepository.delete(facility);
    }
}