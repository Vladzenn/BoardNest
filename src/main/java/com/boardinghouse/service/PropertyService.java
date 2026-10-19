package com.boardinghouse.service;

import com.boardinghouse.model.*;
import com.boardinghouse.repository.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PropertyService {
    private final UserRepository users;
    private final PropertyRepository properties;
    private final FloorRepository floors;
    private final RoomRepository rooms;
    private final TenantRepository tenants;
    private final OccupancyRepository occupancies;

    public PropertyService(UserRepository users, PropertyRepository properties, FloorRepository floors,
            RoomRepository rooms, TenantRepository tenants, OccupancyRepository occupancies) {
        this.users=users; this.properties=properties; this.floors=floors; this.rooms=rooms;
        this.tenants=tenants; this.occupancies=occupancies;
    }

    public record PropertySummary(Long id, String name, String address, int floors, int rooms, long residents, int capacity) {}
    public record TenantCard(Long id, String name, String phone, String email, BigDecimal monthlyRent, Long roomId, String roomNumber) {}
    public record RoomCard(Long id, String number, int capacity, long occupied, List<TenantCard> tenants) {}
    public record FloorCard(Long id, int number, List<RoomCard> rooms) {}
    public record PropertyDetail(Long id, String name, String address, List<FloorCard> floors, List<TenantCard> tenants,
                                 int roomCount, int capacity, long occupied) {}

    @Transactional(readOnly=true)
    public List<PropertySummary> listProperties(String username) {
        return properties.findAllByOwner_UsernameOrderByNameAsc(username).stream().map(p -> {
            List<Floor> pf = floors.findAllByProperty_IdOrderByFloorNumberDesc(p.getId());
            List<Room> pr = rooms.findAllByFloor_Property_IdOrderByFloor_FloorNumberDescRoomNumberAsc(p.getId());
            long occupied = pr.stream().mapToLong(r -> occupancies.countByRoom_Id(r.getId())).sum();
            int capacity = pr.stream().mapToInt(Room::getCapacity).sum();
            return new PropertySummary(p.getId(), p.getName(), p.getAddress(), pf.size(), pr.size(), occupied, capacity);
        }).toList();
    }

    @Transactional
    public Long createProperty(String username, String name, String address) {
        User owner = users.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("Account not found."));
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Enter a property name.");
        return properties.save(new Property(name.trim(), blankToNull(address), owner)).getId();
    }

    @Transactional
    public void addFloor(String username, Long propertyId, int number) {
        Property property = ownedProperty(username, propertyId);
        if (number < 0 || number > 99) throw new IllegalArgumentException("Floor number must be between 0 and 99.");
        if (floors.existsByProperty_IdAndFloorNumber(propertyId, number)) throw new IllegalArgumentException("That floor already exists.");
        floors.save(new Floor(number, property));
    }

    @Transactional
    public void addRoom(String username, Long propertyId, Long floorId, String number, int capacity) {
        ownedProperty(username, propertyId);
        Floor floor = floors.findByIdAndProperty_Id(floorId, propertyId).orElseThrow(() -> new IllegalArgumentException("Floor not found in this property."));
        if (number == null || number.isBlank()) throw new IllegalArgumentException("Enter a room number or name.");
        if (capacity < 1 || capacity > 30) throw new IllegalArgumentException("Room capacity must be between 1 and 30.");
        if (rooms.existsByFloor_IdAndRoomNumber(floorId, number.trim())) throw new IllegalArgumentException("That room already exists on this floor.");
        rooms.save(new Room(number.trim(), capacity, floor));
    }

    @Transactional
    public void addTenant(String username, Long propertyId, String name, String phone, String email) {
        addTenant(username, propertyId, name, phone, email, BigDecimal.ZERO);
    }

    @Transactional
    public void addTenant(String username, Long propertyId, String name, String phone, String email, BigDecimal monthlyRent) {
        Property property = ownedProperty(username, propertyId);
        validateTenantDetails(name, monthlyRent);
        tenants.save(new Tenant(name.trim(), blankToNull(phone), blankToNull(email), monthlyRent, property));
    }

    @Transactional
    public void updateTenant(String username, Long propertyId, Long tenantId, String name,
            String phone, String email, BigDecimal monthlyRent) {
        ownedProperty(username, propertyId);
        Tenant tenant = tenants.findByIdAndProperty_Id(tenantId, propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant does not belong to this property."));
        validateTenantDetails(name, monthlyRent);
        tenant.setFullName(name.trim());
        tenant.setPhone(blankToNull(phone));
        tenant.setEmail(blankToNull(email));
        tenant.setMonthlyRent(monthlyRent);
    }

    @Transactional
    public void deleteTenant(String username, Long propertyId, Long tenantId) {
        ownedProperty(username, propertyId);
        Tenant tenant = tenants.findByIdAndProperty_Id(tenantId, propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant does not belong to this property."));
        occupancies.findByTenant_Id(tenantId).ifPresent(occupancies::delete);
        tenants.delete(tenant);
    }

    private void validateTenantDetails(String name, BigDecimal monthlyRent) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Enter the tenant's name.");
        if (name.trim().length() > 120) throw new IllegalArgumentException("Tenant name must be 120 characters or fewer.");
        if (monthlyRent == null || monthlyRent.signum() < 0 || monthlyRent.scale() > 2 || monthlyRent.precision() - monthlyRent.scale() > 8) {
            throw new IllegalArgumentException("Monthly rent must be a non-negative amount with up to 2 decimal places.");
        }
    }

    @Transactional
    public void assignTenant(String username, Long propertyId, Long tenantId, Long roomId) {
        ownedProperty(username, propertyId);
        Tenant tenant = tenants.findByIdAndProperty_Id(tenantId, propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant does not belong to this property."));
        if (roomId == null) {
            occupancies.findByTenant_Id(tenantId).ifPresent(occupancies::delete);
            return;
        }
        Room room = rooms.findByIdAndFloor_Property_Id(roomId, propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Room does not belong to this property."));
        long count = occupancies.countByRoom_Id(roomId);
        Optional<Occupancy> current = occupancies.findByTenant_Id(tenantId);
        if (current.isPresent() && current.get().getRoom().getId().equals(roomId)) return;
        if (count >= room.getCapacity()) throw new IllegalArgumentException("That room is full. Choose a room with an available space.");
        if (current.isPresent()) {
            current.get().setRoom(room);
        } else {
            occupancies.save(new Occupancy(tenant, room));
        }
    }

    @Transactional(readOnly=true)
    public PropertyDetail getProperty(String username, Long propertyId) {
        Property p = ownedProperty(username, propertyId);
        List<Tenant> allTenants = tenants.findAllByProperty_IdOrderByFullNameAsc(propertyId);
        Map<Long, Occupancy> byTenant = new HashMap<>();
        for (Tenant t : allTenants) occupancies.findByTenant_Id(t.getId()).ifPresent(o -> byTenant.put(t.getId(), o));
        List<TenantCard> tenantCards = allTenants.stream().map(t -> {
            Occupancy o = byTenant.get(t.getId());
            return new TenantCard(t.getId(), t.getFullName(), t.getPhone(), t.getEmail(), t.getMonthlyRent(),
                    o == null ? null : o.getRoom().getId(), o == null ? null : o.getRoom().getRoomNumber());
        }).toList();
        List<FloorCard> floorCards = floors.findAllByProperty_IdOrderByFloorNumberDesc(propertyId).stream().map(f -> {
            List<RoomCard> roomCards = rooms.findAllByFloor_Property_IdOrderByFloor_FloorNumberDescRoomNumberAsc(propertyId).stream()
                    .filter(r -> r.getFloor().getId().equals(f.getId())).map(r -> {
                        List<TenantCard> assigned = tenantCards.stream().filter(t -> Objects.equals(t.roomId(), r.getId())).toList();
                        return new RoomCard(r.getId(), r.getRoomNumber(), r.getCapacity(), assigned.size(), assigned);
                    }).toList();
            return new FloorCard(f.getId(), f.getFloorNumber(), roomCards);
        }).toList();
        int roomCount = floorCards.stream().mapToInt(fc -> fc.rooms().size()).sum();
        int capacity = floorCards.stream().flatMap(fc -> fc.rooms().stream()).mapToInt(RoomCard::capacity).sum();
        long occupied = tenantCards.stream().filter(t -> t.roomId() != null).count();
        return new PropertyDetail(p.getId(), p.getName(), p.getAddress(), floorCards, tenantCards, roomCount, capacity, occupied);
    }

    private Property ownedProperty(String username, Long propertyId) {
        return properties.findByIdAndOwner_Username(propertyId, username)
                .orElseThrow(() -> new IllegalArgumentException("Property not found or you do not have access to it."));
    }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
