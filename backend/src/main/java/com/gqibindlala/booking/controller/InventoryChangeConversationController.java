package com.gqibindlala.booking.controller;

import com.gqibindlala.booking.dto.CreateInventoryChangeMessageRequest;
import com.gqibindlala.booking.dto.InventoryChangeMessageResponse;
import com.gqibindlala.booking.security.AuthPrincipal;
import com.gqibindlala.booking.service.InventoryChangeConversationService;
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
@RequestMapping("/api/inventory-change-requests/{requestId}/messages")
public class InventoryChangeConversationController {
    private final InventoryChangeConversationService conversationService;

    public InventoryChangeConversationController(InventoryChangeConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping
    public List<InventoryChangeMessageResponse> messages(@PathVariable Long requestId,
            @AuthenticationPrincipal AuthPrincipal principal) {
        return conversationService.messages(requestId, principal);
    }

    @PostMapping
    public InventoryChangeMessageResponse send(@PathVariable Long requestId,
            @Valid @RequestBody CreateInventoryChangeMessageRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        return conversationService.send(requestId, request, principal);
    }
}