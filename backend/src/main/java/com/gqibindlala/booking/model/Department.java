package com.gqibindlala.booking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "departments")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(name = "login_domain", nullable = false, unique = true)
    private String loginDomain;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "catalog_visible", nullable = false)
    private boolean catalogVisible = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getLoginDomain() { return loginDomain; }
    public void setLoginDomain(String loginDomain) { this.loginDomain = loginDomain; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isCatalogVisible() { return catalogVisible; }
    public void setCatalogVisible(boolean catalogVisible) { this.catalogVisible = catalogVisible; }
}