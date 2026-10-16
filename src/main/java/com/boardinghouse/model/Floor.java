package com.boardinghouse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

/** A numbered floor within a property. */
@Entity
@Table(name = "floors", uniqueConstraints = {
        @UniqueConstraint(name = "uk_floor_property_number", columnNames = {"property_id", "floor_number"})
})
public class Floor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(0)
    @Column(name = "floor_number", nullable = false)
    private int floorNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @OneToMany(mappedBy = "floor", fetch = FetchType.LAZY)
    private Set<Room> rooms = new LinkedHashSet<>();

    protected Floor() {
        // Required by JPA.
    }

    public Floor(int floorNumber, Property property) {
        this.floorNumber = floorNumber;
        this.property = property;
    }

    public Long getId() { return id; }
    public int getFloorNumber() { return floorNumber; }
    public void setFloorNumber(int floorNumber) { this.floorNumber = floorNumber; }
    public Property getProperty() { return property; }
    public void setProperty(Property property) { this.property = property; }
    public Set<Room> getRooms() { return rooms; }
}
