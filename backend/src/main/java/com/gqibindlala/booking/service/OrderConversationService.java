package com.gqibindlala.booking.service;

import com.gqibindlala.booking.dto.CreateOrderMessageRequest;
import com.gqibindlala.booking.dto.OrderMessageResponse;
import com.gqibindlala.booking.model.AppRole;
import com.gqibindlala.booking.model.AppUser;
import com.gqibindlala.booking.model.BookingRequest;
import com.gqibindlala.booking.model.Department;
import com.gqibindlala.booking.model.OrderMessage;
import com.gqibindlala.booking.model.BookingStatus;
import com.gqibindlala.booking.repository.AppUserRepository;
import com.gqibindlala.booking.repository.BookingRequestRepository;
import com.gqibindlala.booking.repository.DepartmentRepository;
import com.gqibindlala.booking.repository.OrderMessageRepository;
import com.gqibindlala.booking.security.AuthPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class OrderConversationService {
    private final BookingRequestRepository bookingRepository;
    private final OrderMessageRepository messageRepository;
    private final AppUserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public OrderConversationService(BookingRequestRepository bookingRepository,
                                    OrderMessageRepository messageRepository,
                                    AppUserRepository userRepository,
                                    DepartmentRepository departmentRepository) {
        this.bookingRepository = bookingRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderMessageResponse> messages(Long bookingId, AuthPrincipal principal) {
        BookingRequest booking = findAuthorizedBooking(bookingId, principal);
        return messageRepository.findByBookingRequestIdOrderByCreatedAtAsc(bookingId).stream()
            .filter(message -> principal.role() != AppRole.DEPARTMENT
                || message.getDepartment() == null
                || message.getDepartment().getId().equals(principal.departmentId()))
            .map(OrderMessageResponse::from)
            .toList();
    }

    @Transactional
    public OrderMessageResponse send(Long bookingId, CreateOrderMessageRequest request, AuthPrincipal principal) {
        BookingRequest booking = findAuthorizedBooking(bookingId, principal);
        AppUser sender = userRepository.findById(principal.id())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        OrderMessage message = new OrderMessage();
        message.setBookingRequest(booking);
        message.setSender(sender);
        message.setContent(request.content().trim());

        if (principal.role() == AppRole.DEPARTMENT) {
            Department department = departmentRepository.findById(principal.departmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
            message.setDepartment(department);
        }
        return OrderMessageResponse.from(messageRepository.save(message));
    }

    private BookingRequest findAuthorizedBooking(Long bookingId, AuthPrincipal principal) {
        BookingRequest booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (principal.role() == AppRole.CUSTOMER
                && (booking.getCustomer() == null || !booking.getCustomer().getId().equals(principal.id()))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        if (principal.role() == AppRole.DEPARTMENT && booking.getItems().stream().noneMatch(item ->
                item.getStatus() == BookingStatus.CONFIRMED
                    && item.getEquipment().getDepartment().getId().equals(principal.departmentId()))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        return booking;
    }
}