package com.gqibindlala.booking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "inventory_change_requests")
public class InventoryChangeRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private AppUser requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private AppUser reviewedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    private InventoryChangeType changeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryChangeStatus status = InventoryChangeStatus.PENDING;

    @Column(name = "proposed_name")
    private String proposedName;

    @Column(name = "proposed_description", length = 1000)
    private String proposedDescription;

    @Column(name = "proposed_quantity")
    private Integer proposedQuantity;

    @Column(name = "proposed_price", precision = 10, scale = 2)
    private BigDecimal proposedPrice;

    @Column(name = "proposed_image_url", length = 500)
    private String proposedImageUrl;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(name = "review_note", length = 1000)
    private String reviewNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public Equipment getEquipment() { return equipment; }
    public void setEquipment(Equipment equipment) { this.equipment = equipment; }
    public AppUser getRequestedBy() { return requestedBy; }
    public void setRequestedBy(AppUser requestedBy) { this.requestedBy = requestedBy; }
    public AppUser getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(AppUser reviewedBy) { this.reviewedBy = reviewedBy; }
    public InventoryChangeType getChangeType() { return changeType; }
    public void setChangeType(InventoryChangeType changeType) { this.changeType = changeType; }
    public InventoryChangeStatus getStatus() { return status; }
    public void setStatus(InventoryChangeStatus status) { this.status = status; }
    public String getProposedName() { return proposedName; }
    public void setProposedName(String proposedName) { this.proposedName = proposedName; }
    public String getProposedDescription() { return proposedDescription; }
    public void setProposedDescription(String proposedDescription) { this.proposedDescription = proposedDescription; }
    public Integer getProposedQuantity() { return proposedQuantity; }
    public void setProposedQuantity(Integer proposedQuantity) { this.proposedQuantity = proposedQuantity; }
    public BigDecimal getProposedPrice() { return proposedPrice; }
    public void setProposedPrice(BigDecimal proposedPrice) { this.proposedPrice = proposedPrice; }
    public String getProposedImageUrl() { return proposedImageUrl; }
    public void setProposedImageUrl(String proposedImageUrl) { this.proposedImageUrl = proposedImageUrl; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }
}