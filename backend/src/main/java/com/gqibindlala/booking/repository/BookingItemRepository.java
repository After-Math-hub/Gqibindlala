package com.gqibindlala.booking.repository;

import com.gqibindlala.booking.model.BookingItem;
import com.gqibindlala.booking.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingItemRepository extends JpaRepository<BookingItem, Long> {

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
}
