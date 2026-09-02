package com.sarthak.societyfacilitybookingportal.repository;

import com.sarthak.societyfacilitybookingportal.entity.Booking;
import com.sarthak.societyfacilitybookingportal.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    List<Booking> findAllByOrderByBookingIdDesc();

    List<Booking> findByStatusOrderByBookingIdDesc(
            BookingStatus status
    );
}