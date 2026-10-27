package com.gqibindlala.booking.repository;

import com.gqibindlala.booking.model.InventoryChangeRequest;
import com.gqibindlala.booking.model.InventoryChangeStatus;
import com.gqibindlala.booking.model.InventoryChangeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InventoryChangeRequestRepository extends JpaRepository<InventoryChangeRequest, Long> {
    @EntityGraph(attributePaths = {"department", "equipment", "requestedBy", "reviewedBy"})
    List<InventoryChangeRequest> findByStatusOrderByCreatedAtAsc(InventoryChangeStatus status);

    @EntityGraph(attributePaths = {"department", "equipment", "requestedBy", "reviewedBy"})
    List<InventoryChangeRequest> findByDepartmentIdOrderByCreatedAtDesc(Long departmentId);

    boolean existsByEquipmentIdAndChangeTypeAndStatus(Long equipmentId, InventoryChangeType changeType,
                                                       InventoryChangeStatus status);

    @Override
    @EntityGraph(attributePaths = {"department", "equipment", "requestedBy", "reviewedBy"})
    Optional<InventoryChangeRequest> findById(Long id);
}