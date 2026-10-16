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
import jakarta.validation.constraints.Email;
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
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.property = property;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Property getProperty() { return property; }
    public void setProperty(Property property) { this.property = property; }
    public Occupancy getOccupancy() { return occupancy; }
    public void setOccupancy(Occupancy occupancy) { this.occupancy = occupancy; }
}
