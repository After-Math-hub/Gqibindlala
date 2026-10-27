package com.gqibindlala.booking.repository;

import com.gqibindlala.booking.model.BookingItem;
import com.gqibindlala.booking.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingItemRepository extends JpaRepository<BookingItem, Long> {

    @Override
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {
        "bookingRequest", "bookingRequest.items", "bookingRequest.items.equipment",
        "bookingRequest.items.equipment.department"
    })
    Optional<BookingItem> findById(Long id);

    // Sum of quantity already committed to a piece of equipment for dates
    // that overlap the requested range, counting only CONFIRMED items.
    // Pending items deliberately do NOT block availability (first-confirmed wins).
    @Query("""
        SELECT COALESCE(SUM(bi.quantity), 0)
        FROM BookingItem bi
        WHERE bi.equipment.id = :equipmentId
        AND bi.status = :status
        AND bi.startDate <= :endDate
        AND bi.endDate >= :startDate
        """)
    Integer sumBookedQuantityForRange(
        @Param("equipmentId") Long equipmentId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("status") BookingStatus status
    );

    List<BookingItem> findByEquipmentIdAndStatus(Long equipmentId, BookingStatus status);

    @Query("""
        SELECT DISTINCT booking
        FROM BookingItem bi
        JOIN bi.bookingRequest booking
        LEFT JOIN FETCH booking.items allItems
        LEFT JOIN FETCH allItems.equipment equipment
        LEFT JOIN FETCH equipment.department
        WHERE bi.equipment.department.id = :departmentId
        AND booking.status = :status
        ORDER BY booking.createdAt DESC
        """)
    List<com.gqibindlala.booking.model.BookingRequest> findDepartmentBookings(
        @Param("departmentId") Long departmentId,
        @Param("status") BookingStatus status
    );
}
