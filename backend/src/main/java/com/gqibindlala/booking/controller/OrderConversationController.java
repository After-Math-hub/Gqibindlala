package com.gqibindlala.booking.controller;

import com.gqibindlala.booking.dto.CreateOrderMessageRequest;
import com.gqibindlala.booking.dto.OrderMessageResponse;
import com.gqibindlala.booking.security.AuthPrincipal;
import com.gqibindlala.booking.service.OrderConversationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/bookings/{bookingId}/messages")
public class OrderConversationController {
    private final OrderConversationService conversationService;

    public OrderConversationController(OrderConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping
    public List<OrderMessageResponse> messages(@PathVariable Long bookingId,
            @AuthenticationPrincipal AuthPrincipal principal) {
        return conversationService.messages(bookingId, principal);
    }

    @PostMapping
    public OrderMessageResponse send(@PathVariable Long bookingId,
            @Valid @RequestBody CreateOrderMessageRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        return conversationService.send(bookingId, request, principal);
    }
}