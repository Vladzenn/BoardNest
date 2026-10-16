package com.boardinghouse.repository;

import com.boardinghouse.model.Room;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findAllByFloor_Property_IdOrderByFloor_FloorNumberDescRoomNumberAsc(Long propertyId);
    Optional<Room> findByIdAndFloor_Property_Id(Long id, Long propertyId);
    boolean existsByFloor_IdAndRoomNumber(Long floorId, String roomNumber);
}
