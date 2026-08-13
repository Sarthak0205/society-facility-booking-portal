package com.sarthak.societyfacilitybookingportal.repository;

import com.sarthak.societyfacilitybookingportal.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}