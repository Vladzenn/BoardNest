package com.boardinghouse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** A tenant belongs to a property independently of their current room assignment. */
@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Size(max = 30)
    @Column(length = 30)
    private String phone;

    @Email
    @Size(max = 120)
    @Column(length = 120)
    private String email;

    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "monthly_rent", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyRent = BigDecimal.ZERO;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    /** Null when the tenant is currently unassigned. */
    @OneToOne(mappedBy = "tenant", fetch = FetchType.LAZY)
    private Occupancy occupancy;

    protected Tenant() {
        // Required by JPA.
    }

    public Tenant(String fullName, String phone, String email, Property property) {
        this(fullName, phone, email, BigDecimal.ZERO, property);
    }

    public Tenant(String fullName, String phone, String email, BigDecimal monthlyRent, Property property) {
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.monthlyRent = monthlyRent == null ? BigDecimal.ZERO : monthlyRent;
        this.property = property;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public BigDecimal getMonthlyRent() { return monthlyRent; }
    public void setMonthlyRent(BigDecimal monthlyRent) { this.monthlyRent = monthlyRent; }
    public Property getProperty() { return property; }
    public void setProperty(Property property) { this.property = property; }
    public Occupancy getOccupancy() { return occupancy; }
    public void setOccupancy(Occupancy occupancy) { this.occupancy = occupancy; }
}
