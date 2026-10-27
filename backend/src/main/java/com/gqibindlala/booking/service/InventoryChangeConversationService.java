package com.gqibindlala.booking.service;

import com.gqibindlala.booking.dto.CreateInventoryChangeMessageRequest;
import com.gqibindlala.booking.dto.InventoryChangeMessageResponse;
import com.gqibindlala.booking.model.AppRole;
import com.gqibindlala.booking.model.AppUser;
import com.gqibindlala.booking.model.InventoryChangeMessage;
import com.gqibindlala.booking.model.InventoryChangeRequest;
import com.gqibindlala.booking.repository.AppUserRepository;
import com.gqibindlala.booking.repository.InventoryChangeMessageRepository;
import com.gqibindlala.booking.repository.InventoryChangeRequestRepository;
import com.gqibindlala.booking.security.AuthPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class InventoryChangeConversationService {
    private final InventoryChangeRequestRepository requestRepository;
    private final InventoryChangeMessageRepository messageRepository;
    private final AppUserRepository userRepository;

    public InventoryChangeConversationService(InventoryChangeRequestRepository requestRepository,
                                              InventoryChangeMessageRepository messageRepository,
                                              AppUserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<InventoryChangeMessageResponse> messages(Long requestId, AuthPrincipal principal) {
        requireStaff(principal);
        findAuthorizedRequest(requestId, principal);
        return messageRepository.findByChangeRequestIdOrderByCreatedAtAsc(requestId).stream()
            .map(InventoryChangeMessageResponse::from).toList();
    }

    @Transactional
    public InventoryChangeMessageResponse send(Long requestId, CreateInventoryChangeMessageRequest input,
                                                AuthPrincipal principal) {
        requireStaff(principal);
        InventoryChangeRequest request = findAuthorizedRequest(requestId, principal);
        AppUser sender = userRepository.findById(principal.id())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        InventoryChangeMessage message = new InventoryChangeMessage();
        message.setChangeRequest(request);
        message.setSender(sender);
        message.setContent(input.content().trim());
        return InventoryChangeMessageResponse.from(messageRepository.save(message));
    }

    private void requireStaff(AuthPrincipal principal) {
        if (principal.role() == AppRole.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private InventoryChangeRequest findAuthorizedRequest(Long requestId, AuthPrincipal principal) {
        InventoryChangeRequest request = requestRepository.findById(requestId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Change request not found"));
        if (principal.role() == AppRole.DEPARTMENT
                && !request.getDepartment().getId().equals(principal.departmentId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Change request not found");
        }
        return request;
    }
}