package com.gqibindlala.booking.service;

import com.gqibindlala.booking.dto.BookingItemRequest;
import com.gqibindlala.booking.dto.CreateBookingRequest;
import com.gqibindlala.booking.model.*;
import com.gqibindlala.booking.repository.BookingRequestRepository;
import com.gqibindlala.booking.repository.EquipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

@Service
public class BookingService {

    private final BookingRequestRepository bookingRequestRepository;
    private final EquipmentRepository equipmentRepository;
    private final AvailabilityService availabilityService;

    public BookingService(BookingRequestRepository bookingRequestRepository,
                           EquipmentRepository equipmentRepository,
                           AvailabilityService availabilityService) {
        this.bookingRequestRepository = bookingRequestRepository;
        this.equipmentRepository = equipmentRepository;
        this.availabilityService = availabilityService;
    }

    @Transactional
    public BookingRequest createBooking(CreateBookingRequest request) {
        BookingRequest booking = new BookingRequest();
        booking.setCustomerName(request.getCustomerName());
        booking.setCustomerContact(request.getCustomerContact());
        booking.setStatus(BookingStatus.PENDING);

        for (BookingItemRequest itemReq : request.getItems()) {
            Equipment equipment = equipmentRepository.findById(itemReq.getEquipmentId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Equipment not found: " + itemReq.getEquipmentId()));

            // Pending bookings don't reserve stock, so this is an early heads-up
            // to the customer rather than a hard lock. Re-checked again at confirm time.
            if (!availabilityService.isAvailable(
                    equipment.getId(), itemReq.getStartDate(), itemReq.getEndDate(), itemReq.getQuantity())) {
                throw new IllegalStateException(
                    "Not enough " + equipment.getName() + " available for the selected dates");
            }

            BookingItem item = new BookingItem();
            item.setBookingRequest(booking);
            item.setEquipment(equipment);
            item.setQuantity(itemReq.getQuantity());
            item.setStartDate(itemReq.getStartDate());
            item.setEndDate(itemReq.getEndDate());
            item.setUnitPrice(equipment.getPricePerUnitPerDay());
            item.setStatus(BookingStatus.PENDING);
            item.setLineTotal(calculateLineTotal(
                equipment.getPricePerUnitPerDay(), itemReq.getQuantity(),
                itemReq.getStartDate(), itemReq.getEndDate()));

            booking.getItems().add(item);
        }

        return bookingRequestRepository.save(booking);
    }

    /**
     * Confirms payment for a booking. Re-validates availability at confirm time
     * (not just at request time) since stock may have been taken by another
     * customer who confirmed first.
     */
    @Transactional
    public BookingRequest confirmBooking(Long bookingId) {
        BookingRequest booking = bookingRequestRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        for (BookingItem item : booking.getItems()) {
            if (!availabilityService.isAvailable(
                    item.getEquipment().getId(), item.getStartDate(), item.getEndDate(), item.getQuantity())) {
                throw new IllegalStateException(
                    "Not enough " + item.getEquipment().getName() + " available to confirm this booking");
            }
            item.setStatus(BookingStatus.CONFIRMED);
        }
        booking.setStatus(BookingStatus.CONFIRMED);

        return bookingRequestRepository.save(booking);
    }

    @Transactional
    public BookingRequest cancelBooking(Long bookingId) {
        BookingRequest booking = bookingRequestRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        booking.getItems().forEach(item -> item.setStatus(BookingStatus.CANCELLED));
        booking.setStatus(BookingStatus.CANCELLED);

        return bookingRequestRepository.save(booking);
    }

    private BigDecimal calculateLineTotal(BigDecimal pricePerDay, int quantity,
                                           java.time.LocalDate start, java.time.LocalDate end) {
        long days = Math.max(1, ChronoUnit.DAYS.between(start, end) + 1);
        return pricePerDay
            .multiply(BigDecimal.valueOf(quantity))
            .multiply(BigDecimal.valueOf(days));
    }
}
