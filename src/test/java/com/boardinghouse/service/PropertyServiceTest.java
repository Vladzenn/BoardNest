package com.boardinghouse.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.boardinghouse.model.*;
import com.boardinghouse.repository.*;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {
    @Mock UserRepository users;
    @Mock PropertyRepository properties;
    @Mock FloorRepository floors;
    @Mock RoomRepository rooms;
    @Mock TenantRepository tenants;
    @Mock OccupancyRepository occupancies;
    PropertyService service;
    Property property;
    Tenant tenant;
    Room targetRoom;

    @BeforeEach void setUp() {
        service = new PropertyService(users, properties, floors, rooms, tenants, occupancies);
        User owner = new User("Owner", "owner", "encoded");
        property = new Property("House", null, owner);
        Floor floor = new Floor(1, property);
        targetRoom = new Room("101", 1, floor);
        tenant = new Tenant("Tenant One", null, null, property);
    }

    @Test void rejectsAssignmentWhenRoomIsFullWithoutChangingOccupancy() {
        when(properties.findByIdAndOwner_Username(10L, "owner")).thenReturn(Optional.of(property));
        when(tenants.findByIdAndProperty_Id(20L, 10L)).thenReturn(Optional.of(tenant));
        when(rooms.findByIdAndFloor_Property_Id(30L, 10L)).thenReturn(Optional.of(targetRoom));
        when(occupancies.countByRoom_Id(30L)).thenReturn(1L);
        when(occupancies.findByTenant_Id(20L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.assignTenant("owner", 10L, 20L, 30L));
        verify(occupancies, never()).save(any(Occupancy.class));
        verify(occupancies, never()).delete(any(Occupancy.class));
    }

    @Test void rejectsPropertyNotOwnedByCurrentUser() {
        when(properties.findByIdAndOwner_Username(10L, "someone-else")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.addFloor("someone-else", 10L, 2));
        verify(floors, never()).save(any(Floor.class));
    }
}
