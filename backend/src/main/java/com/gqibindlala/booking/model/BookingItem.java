package com.gqibindlala.booking.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "booking_items")
public class BookingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_request_id", nullable = false)
    private BookingRequest bookingRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // price per unit per day, snapshotted at time of booking
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "line_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal;

    // an item can be confirmed/cancelled independently of the parent booking
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_status", nullable = false)
    private FulfillmentStatus fulfillmentStatus = FulfillmentStatus.NOT_STARTED;

    @Column(name = "quotation_issued_at")
    private java.time.Instant quotationIssuedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_issued_by_id")
    private AppUser quotationIssuedBy;

    // --- getters / setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BookingRequest getBookingRequest() { return bookingRequest; }
    public void setBookingRequest(BookingRequest bookingRequest) { this.bookingRequest = bookingRequest; }

    public Equipment getEquipment() { return equipment; }
    public void setEquipment(Equipment equipment) { this.equipment = equipment; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public FulfillmentStatus getFulfillmentStatus() { return fulfillmentStatus; }
    public void setFulfillmentStatus(FulfillmentStatus fulfillmentStatus) { this.fulfillmentStatus = fulfillmentStatus; }
    public java.time.Instant getQuotationIssuedAt() { return quotationIssuedAt; }
    public void setQuotationIssuedAt(java.time.Instant quotationIssuedAt) { this.quotationIssuedAt = quotationIssuedAt; }
    public AppUser getQuotationIssuedBy() { return quotationIssuedBy; }
    public void setQuotationIssuedBy(AppUser quotationIssuedBy) { this.quotationIssuedBy = quotationIssuedBy; }
}
