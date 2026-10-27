package com.gqibindlala.booking.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "equipment")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(length = 1000)
    private String description;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @Column(name = "price_per_unit_per_day", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerUnitPerDay;

    @Column(name = "image_url")
    private String imageUrl;

    // --- getters / setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }

    public BigDecimal getPricePerUnitPerDay() { return pricePerUnitPerDay; }
    public void setPricePerUnitPerDay(BigDecimal pricePerUnitPerDay) { this.pricePerUnitPerDay = pricePerUnitPerDay; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
