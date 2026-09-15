package com.gqibindlala.booking.controller;

import com.gqibindlala.booking.dto.CreateBookingRequest;
import com.gqibindlala.booking.model.BookingRequest;
import com.gqibindlala.booking.repository.BookingRequestRepository;
import com.gqibindlala.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final BookingRequestRepository bookingRequestRepository;

    public BookingController(BookingService bookingService,
                              BookingRequestRepository bookingRequestRepository) {
        this.bookingService = bookingService;
        this.bookingRequestRepository = bookingRequestRepository;
    }

    // Public - customer submits a booking/quote request
    @PostMapping
    public BookingRequest create(@Valid @RequestBody CreateBookingRequest request) {
        return bookingService.createBooking(request);
    }

    // Admin - list all bookings
    @GetMapping
    public List<BookingRequest> listAll() {
        return bookingRequestRepository.findAll();
    }

    // Admin - confirm payment, locks in availability
    @PostMapping("/{id}/confirm")
    public BookingRequest confirm(@PathVariable Long id) {
        return bookingService.confirmBooking(id);
    }

    // Admin - cancel a booking
    @PostMapping("/{id}/cancel")
    public BookingRequest cancel(@PathVariable Long id) {
        return bookingService.cancelBooking(id);
    }
}
