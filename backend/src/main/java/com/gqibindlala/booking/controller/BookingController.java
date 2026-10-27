package com.gqibindlala.booking.controller;

import com.gqibindlala.booking.dto.CreateBookingRequest;
import com.gqibindlala.booking.dto.BookingResponse;
import com.gqibindlala.booking.dto.FulfillmentUpdateRequest;
import com.gqibindlala.booking.dto.PaymentReferenceRequest;
import com.gqibindlala.booking.dto.FinanceRequestStatusUpdate;
import com.gqibindlala.booking.model.BookingItem;
import com.gqibindlala.booking.model.BookingRequest;
import com.gqibindlala.booking.security.AuthPrincipal;
import com.gqibindlala.booking.repository.BookingRequestRepository;
import com.gqibindlala.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @PostMapping
    public BookingResponse create(@Valid @RequestBody CreateBookingRequest request,
                                  @AuthenticationPrincipal AuthPrincipal principal) {
        return BookingResponse.from(bookingService.createBooking(request, principal));
    }

    @GetMapping("/mine")
    public List<BookingResponse> mine(@AuthenticationPrincipal AuthPrincipal principal) {
        return bookingService.customerOrders(principal.id()).stream().map(BookingResponse::from).toList();
    }

    @PostMapping("/{id}/payment-reference")
    public BookingResponse submitPaymentReference(@PathVariable Long id,
            @Valid @RequestBody PaymentReferenceRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        return BookingResponse.from(bookingService.submitPaymentReference(id, request.reference(), principal));
    }

    @GetMapping("/finance-requests")
    public List<BookingResponse> financeRequests() {
        return bookingService.financeRequestQueue().stream().map(BookingResponse::from).toList();
    }

    @PatchMapping("/{id}/finance-request")
    public BookingResponse updateFinanceRequest(@PathVariable Long id,
            @Valid @RequestBody FinanceRequestStatusUpdate request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        return BookingResponse.from(bookingService.updateFinanceRequestStatus(id, request.status(), principal));
    }

    @GetMapping("/payment-review")
    public List<BookingResponse> paymentReviewQueue() {
        return bookingService.paymentReviewQueue().stream().map(BookingResponse::from).toList();
    }

    @PostMapping("/{id}/payment-review/approve")
    public BookingResponse approvePayment(@PathVariable Long id,
            @AuthenticationPrincipal AuthPrincipal principal) {
        return BookingResponse.from(bookingService.confirmPayment(id, principal, true));
    }

    @PostMapping("/{id}/payment-review/reject")
    public BookingResponse rejectPayment(@PathVariable Long id,
            @AuthenticationPrincipal AuthPrincipal principal) {
        return BookingResponse.from(bookingService.confirmPayment(id, principal, false));
    }

    @GetMapping("/department")
    public List<BookingResponse> departmentOrders(@AuthenticationPrincipal AuthPrincipal principal) {
        Long departmentId = principal.departmentId();
        return bookingService.departmentOrders(departmentId).stream()
            .map(booking -> BookingResponse.forDepartment(booking, departmentId)).toList();
    }

    @PatchMapping("/items/{itemId}/fulfillment")
    public BookingResponse updateFulfillment(@PathVariable Long itemId,
            @Valid @RequestBody FulfillmentUpdateRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        BookingItem item = bookingService.updateFulfillment(itemId, request.status(), principal);
        return BookingResponse.forDepartment(item.getBookingRequest(), item.getEquipment().getDepartment().getId());
    }

    @PostMapping("/items/{itemId}/quotation")
    public BookingResponse issueQuotation(@PathVariable Long itemId,
            @AuthenticationPrincipal AuthPrincipal principal) {
        BookingItem item = bookingService.issueQuotation(itemId, principal);
        return BookingResponse.from(item.getBookingRequest());
    }

    @GetMapping
    public List<BookingResponse> listAll() {
        return bookingRequestRepository.findAll().stream().map(BookingResponse::from).toList();
    }

    @PostMapping("/{id}/cancel")
    public BookingResponse cancel(@PathVariable Long id) {
        return BookingResponse.from(bookingService.cancelBooking(id));
    }
}
