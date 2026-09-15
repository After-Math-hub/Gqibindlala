package com.gqibindlala.booking.repository;

import com.gqibindlala.booking.model.BookingRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRequestRepository extends JpaRepository<BookingRequest, Long> {
}
