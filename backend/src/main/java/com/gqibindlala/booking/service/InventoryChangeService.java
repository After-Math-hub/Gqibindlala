package com.gqibindlala.booking.service;

import com.gqibindlala.booking.dto.InventoryChangeRequestInput;
import com.gqibindlala.booking.dto.ReviewInventoryChangeRequest;
import com.gqibindlala.booking.model.AppRole;
import com.gqibindlala.booking.model.AppUser;
import com.gqibindlala.booking.model.Department;
import com.gqibindlala.booking.model.Equipment;
import com.gqibindlala.booking.model.InventoryChangeRequest;
import com.gqibindlala.booking.model.InventoryChangeStatus;
import com.gqibindlala.booking.model.InventoryChangeType;
import com.gqibindlala.booking.repository.AppUserRepository;
import com.gqibindlala.booking.repository.DepartmentRepository;
import com.gqibindlala.booking.repository.EquipmentRepository;
import com.gqibindlala.booking.repository.InventoryChangeRequestRepository;
import com.gqibindlala.booking.security.AuthPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.List;

@Service
public class InventoryChangeService {
    private final InventoryChangeRequestRepository requestRepository;
    private final DepartmentRepository departmentRepository;
    private final EquipmentRepository equipmentRepository;
    private final AppUserRepository userRepository;

    public InventoryChangeService(InventoryChangeRequestRepository requestRepository,
                                  DepartmentRepository departmentRepository,
                                  EquipmentRepository equipmentRepository,
                                  AppUserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.departmentRepository = departmentRepository;
        this.equipmentRepository = equipmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public InventoryChangeRequest create(InventoryChangeRequestInput input, AuthPrincipal principal) {
        if (principal.role() != AppRole.DEPARTMENT || principal.departmentId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only department staff can request inventory changes");
        }
        Department department = departmentRepository.findById(principal.departmentId())
            .filter(Department::isActive)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Department is inactive"));
        AppUser requester = userRepository.findById(principal.id())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        InventoryChangeRequest request = new InventoryChangeRequest();
        request.setDepartment(department);
        request.setRequestedBy(requester);
        request.setChangeType(input.changeType());
        request.setReason(input.reason().trim());

        switch (input.changeType()) {
            case ADD_EQUIPMENT -> validateNewEquipment(input, request);
            case PRICE_CHANGE, STOCK_CHANGE -> attachExistingEquipment(input, request, department);
        }
        return requestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<InventoryChangeRequest> departmentRequests(AuthPrincipal principal) {
        if (principal.role() != AppRole.DEPARTMENT || principal.departmentId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return requestRepository.findByDepartmentIdOrderByCreatedAtDesc(principal.departmentId());
    }

    @Transactional(readOnly = true)
    public List<InventoryChangeRequest> pendingRequests() {
        return requestRepository.findByStatusOrderByCreatedAtAsc(InventoryChangeStatus.PENDING);
    }

    @Transactional
    public InventoryChangeRequest review(Long requestId, ReviewInventoryChangeRequest review,
                                         AuthPrincipal principal) {
        if (principal.role() != AppRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        InventoryChangeRequest request = requestRepository.findById(requestId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Change request not found"));
        if (request.getStatus() != InventoryChangeStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Change request has already been reviewed");
        }
        AppUser reviewer = userRepository.findById(principal.id())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (review.approve()) {
            applyApprovedChange(request);
            request.setStatus(InventoryChangeStatus.APPROVED);
        } else {
            request.setStatus(InventoryChangeStatus.REJECTED);
        }
        request.setReviewedBy(reviewer);
        request.setReviewedAt(Instant.now());
        request.setReviewNote(review.reviewNote() == null ? null : review.reviewNote().trim());
        return requestRepository.save(request);
    }

    private void validateNewEquipment(InventoryChangeRequestInput input, InventoryChangeRequest request) {
        if (input.equipmentId() != null || input.name() == null || input.name().isBlank()
                || input.quantity() == null || input.quantity() < 0
                || input.pricePerUnitPerDay() == null || input.pricePerUnitPerDay().signum() < 0
                || input.imageUrl() == null || input.imageUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "New equipment needs a name, photo, non-negative quantity, and non-negative price");
        }
        request.setProposedName(input.name().trim());
        request.setProposedDescription(input.description());
        request.setProposedQuantity(input.quantity());
        request.setProposedPrice(input.pricePerUnitPerDay());
        request.setProposedImageUrl(input.imageUrl().trim());
    }

    private void attachExistingEquipment(InventoryChangeRequestInput input, InventoryChangeRequest request,
                                         Department department) {
        if (input.equipmentId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose an equipment item");
        }
        Equipment equipment = equipmentRepository.findById(input.equipmentId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment not found"));
        if (!equipment.getDepartment().getId().equals(department.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This item belongs to another department");
        }
        if (requestRepository.existsByEquipmentIdAndChangeTypeAndStatus(
                equipment.getId(), input.changeType(), InventoryChangeStatus.PENDING)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A change of this type is already awaiting review");
        }
        if (input.changeType() == InventoryChangeType.PRICE_CHANGE) {
            if (input.pricePerUnitPerDay() == null || input.pricePerUnitPerDay().signum() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a non-negative proposed price");
            }
            request.setProposedPrice(input.pricePerUnitPerDay());
        } else {
            if (input.quantity() == null || input.quantity() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a non-negative proposed quantity");
            }
            request.setProposedQuantity(input.quantity());
        }
        request.setEquipment(equipment);
    }

    private void applyApprovedChange(InventoryChangeRequest request) {
        if (request.getChangeType() == InventoryChangeType.ADD_EQUIPMENT) {
            if (!request.getDepartment().isActive()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Reactivate this department before approving its equipment");
            }
            Equipment equipment = new Equipment();
            equipment.setName(request.getProposedName());
            equipment.setDescription(request.getProposedDescription());
            equipment.setTotalQuantity(request.getProposedQuantity());
            equipment.setPricePerUnitPerDay(request.getProposedPrice());
            equipment.setImageUrl(request.getProposedImageUrl());
            equipment.setDepartment(request.getDepartment());
            request.setEquipment(equipmentRepository.save(equipment));
            return;
        }
        if (request.getChangeType() == InventoryChangeType.PRICE_CHANGE) {
            request.getEquipment().setPricePerUnitPerDay(request.getProposedPrice());
        } else {
            request.getEquipment().setTotalQuantity(request.getProposedQuantity());
        }
        equipmentRepository.save(request.getEquipment());
    }
}