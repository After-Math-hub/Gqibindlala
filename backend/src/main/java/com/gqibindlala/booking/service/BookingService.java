package com.gqibindlala.booking.service;

import com.gqibindlala.booking.dto.BookingItemRequest;
import com.gqibindlala.booking.dto.CreateBookingRequest;
import com.gqibindlala.booking.model.AppRole;
import com.gqibindlala.booking.model.AppUser;
import com.gqibindlala.booking.model.*;
import com.gqibindlala.booking.repository.AppUserRepository;
import com.gqibindlala.booking.repository.BookingItemRepository;
import com.gqibindlala.booking.repository.BookingRequestRepository;
import com.gqibindlala.booking.repository.EquipmentRepository;
import com.gqibindlala.booking.security.AuthPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Comparator;
import java.time.temporal.ChronoUnit;

@Service
public class BookingService {

    private final BookingRequestRepository bookingRequestRepository;
    private final EquipmentRepository equipmentRepository;
    private final AvailabilityService availabilityService;
    private final BookingItemRepository bookingItemRepository;
    private final AppUserRepository userRepository;

    public BookingService(BookingRequestRepository bookingRequestRepository,
                           EquipmentRepository equipmentRepository,
                           AvailabilityService availabilityService,
                           BookingItemRepository bookingItemRepository,
                           AppUserRepository userRepository) {
        this.bookingRequestRepository = bookingRequestRepository;
        this.equipmentRepository = equipmentRepository;
        this.availabilityService = availabilityService;
        this.bookingItemRepository = bookingItemRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BookingRequest createBooking(CreateBookingRequest request, AuthPrincipal principal) {
        BookingRequest booking = new BookingRequest();
        AppUser customer = userRepository.findById(principal.id())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        booking.setCustomer(customer);
        booking.setCustomerName(customer.getFullName());
        booking.setCustomerContact(customer.getPhone());
        booking.setStatus(BookingStatus.PENDING);
        booking.setPaymentStatus(PaymentStatus.UNPAID);

        for (BookingItemRequest itemReq : request.getItems()) {
            if (itemReq.getStartDate().isBefore(java.time.LocalDate.now())
                    || itemReq.getEndDate().isBefore(itemReq.getStartDate())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a valid future date range");
            }
            Equipment equipment = equipmentRepository.findById(itemReq.getEquipmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Equipment not found: " + itemReq.getEquipmentId()));

            // Pending bookings don't reserve stock, so this is an early heads-up
            // to the customer rather than a hard lock. Re-checked again at confirm time.
            if (!availabilityService.isAvailable(
                    equipment.getId(), itemReq.getStartDate(), itemReq.getEndDate(), itemReq.getQuantity())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
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
    public BookingRequest confirmPayment(Long bookingId, AuthPrincipal reviewer, boolean approved) {
        BookingRequest booking = bookingRequestRepository.findById(bookingId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (booking.getPaymentStatus() != PaymentStatus.SUBMITTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This order has no payment awaiting review");
        }

        AppUser reviewerUser = userRepository.findById(reviewer.id())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        booking.setPaymentReviewedBy(reviewerUser);
        booking.setPaymentReviewedAt(java.time.Instant.now());

        if (!approved) {
            booking.setPaymentStatus(PaymentStatus.REJECTED);
            return bookingRequestRepository.save(booking);
        }

        booking.getItems().stream()
            .map(item -> item.getEquipment().getId())
            .distinct()
            .sorted(Comparator.naturalOrder())
            .forEach(equipmentId -> equipmentRepository.findByIdForUpdate(equipmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment not found")));

        for (BookingItem item : booking.getItems()) {
            if (!availabilityService.isAvailable(
                    item.getEquipment().getId(), item.getStartDate(), item.getEndDate(), item.getQuantity())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Not enough " + item.getEquipment().getName() + " available to confirm this order");
            }
            item.setStatus(BookingStatus.CONFIRMED);
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus(PaymentStatus.PAID);

        return bookingRequestRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public java.util.List<BookingRequest> customerOrders(Long customerId) {
        return bookingRequestRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Transactional
    public BookingRequest submitPaymentReference(Long bookingId, String reference, AuthPrincipal customer) {
        BookingRequest booking = bookingRequestRepository.findById(bookingId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (booking.getCustomer() == null || !booking.getCustomer().getId().equals(customer.id())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        if (booking.getStatus() != BookingStatus.PENDING
                || booking.getPaymentStatus() == PaymentStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This order cannot accept a payment reference");
        }
        booking.setPaymentReference(reference.trim());
        booking.setPaymentStatus(PaymentStatus.SUBMITTED);
        booking.setPaymentReviewedAt(null);
        booking.setPaymentReviewedBy(null);
        return bookingRequestRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public java.util.List<BookingRequest> paymentReviewQueue() {
        return bookingRequestRepository.findByPaymentStatusOrderByCreatedAtAsc(PaymentStatus.SUBMITTED);
    }

    @Transactional(readOnly = true)
    public java.util.List<BookingRequest> departmentOrders(Long departmentId) {
        return bookingItemRepository.findDepartmentBookings(departmentId, BookingStatus.CONFIRMED);
    }

    @Transactional
    public BookingItem updateFulfillment(Long itemId, FulfillmentStatus status, AuthPrincipal principal) {
        BookingItem item = bookingItemRepository.findById(itemId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found"));
        if (principal.role() == AppRole.DEPARTMENT
            && !item.getEquipment().getDepartment().getId().equals(principal.departmentId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This item belongs to another department");
        }
        if (item.getStatus() != BookingStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only paid orders can be prepared");
        }
        item.setFulfillmentStatus(status);
        return bookingItemRepository.save(item);
    }

    @Transactional
    public BookingRequest cancelBooking(Long bookingId) {
        BookingRequest booking = bookingRequestRepository.findById(bookingId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        booking.getItems().forEach(item -> item.setStatus(BookingStatus.CANCELLED));
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setPaymentStatus(PaymentStatus.CANCELLED);

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
