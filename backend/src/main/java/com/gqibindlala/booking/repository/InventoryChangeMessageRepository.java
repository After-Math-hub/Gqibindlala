package com.gqibindlala.booking.repository;

import com.gqibindlala.booking.model.InventoryChangeMessage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryChangeMessageRepository extends JpaRepository<InventoryChangeMessage, Long> {
    @EntityGraph(attributePaths = {"changeRequest", "sender"})
    List<InventoryChangeMessage> findByChangeRequestIdOrderByCreatedAtAsc(Long requestId);
}