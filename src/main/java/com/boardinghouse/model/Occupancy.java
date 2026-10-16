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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/** The tenant's single current room assignment. Reassignment updates this record. */
@Entity
@Table(name = "occupancies", uniqueConstraints = {
        @UniqueConstraint(name = "uk_occupancy_tenant", columnNames = "tenant_id")
})
public class Occupancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false, unique = true)
    private Tenant tenant;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @NotNull
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    protected Occupancy() {
        // Required by JPA.
    }

    public Occupancy(Tenant tenant, Room room) {
        this.tenant = tenant;
        this.room = room;
    }

    @PrePersist
    protected void setAssignedAtIfMissing() {
        if (assignedAt == null) {
            assignedAt = Instant.now();
        }
    }

    public Long getId() { return id; }
    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public Instant getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Instant assignedAt) { this.assignedAt = assignedAt; }
}
