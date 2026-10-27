package com.gqibindlala.booking.controller;

import com.gqibindlala.booking.dto.InventoryChangeRequestInput;
import com.gqibindlala.booking.dto.InventoryChangeResponse;
import com.gqibindlala.booking.dto.ReviewInventoryChangeRequest;
import com.gqibindlala.booking.security.AuthPrincipal;
import com.gqibindlala.booking.service.InventoryChangeService;
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
@RequestMapping("/api/inventory-change-requests")
public class InventoryChangeController {
    private final InventoryChangeService changeService;

    public InventoryChangeController(InventoryChangeService changeService) {
        this.changeService = changeService;
    }

    @PostMapping
    public InventoryChangeResponse create(@Valid @RequestBody InventoryChangeRequestInput request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        return InventoryChangeResponse.from(changeService.create(request, principal));
    }

    @GetMapping("/mine")
    public List<InventoryChangeResponse> mine(@AuthenticationPrincipal AuthPrincipal principal) {
        return changeService.departmentRequests(principal).stream().map(InventoryChangeResponse::from).toList();
    }

    @GetMapping
    public List<InventoryChangeResponse> pending() {
        return changeService.pendingRequests().stream().map(InventoryChangeResponse::from).toList();
    }

    @PostMapping("/{id}/review")
    public InventoryChangeResponse review(@PathVariable Long id,
            @Valid @RequestBody ReviewInventoryChangeRequest review,
            @AuthenticationPrincipal AuthPrincipal principal) {
        return InventoryChangeResponse.from(changeService.review(id, review, principal));
    }
}