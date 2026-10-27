package com.gqibindlala.booking.repository;

import com.gqibindlala.booking.model.OrderMessage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderMessageRepository extends JpaRepository<OrderMessage, Long> {
    @EntityGraph(attributePaths = {"bookingRequest", "sender", "department"})
    List<OrderMessage> findByBookingRequestIdOrderByCreatedAtAsc(Long bookingId);
}