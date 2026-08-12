package com.sarthak.societyfacilitybookingportal.repository;

import com.sarthak.societyfacilitybookingportal.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacilityRepository extends JpaRepository<Facility, Long> {
}