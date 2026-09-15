package com.gqibindlala.booking.service;

import com.gqibindlala.booking.model.BookingStatus;
import com.gqibindlala.booking.model.Equipment;
import com.gqibindlala.booking.repository.BookingItemRepository;
import com.gqibindlala.booking.repository.EquipmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AvailabilityService {

    private final EquipmentRepository equipmentRepository;
    private final BookingItemRepository bookingItemRepository;

    public AvailabilityService(EquipmentRepository equipmentRepository,
                                BookingItemRepository bookingItemRepository) {
        this.equipmentRepository = equipmentRepository;
        this.bookingItemRepository = bookingItemRepository;
    }

    /**
     * How many units of this equipment are still free for the given date range.
     * Only CONFIRMED (paid) bookings count against stock — pending requests do not
     * reserve stock, so two customers can both enquire before one pays and confirms.
     */
    public int remainingQuantity(Long equipmentId, LocalDate startDate, LocalDate endDate) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
            .orElseThrow(() -> new IllegalArgumentException("Equipment not found: " + equipmentId));

        int booked = bookingItemRepository.sumBookedQuantityForRange(
            equipmentId, startDate, endDate, BookingStatus.CONFIRMED
        );

        return Math.max(0, equipment.getTotalQuantity() - booked);
    }

    public boolean isAvailable(Long equipmentId, LocalDate startDate, LocalDate endDate, int requestedQuantity) {
        return remainingQuantity(equipmentId, startDate, endDate) >= requestedQuantity;
    }
}
