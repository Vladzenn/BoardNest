package com.boardinghouse.repository;

import com.boardinghouse.model.Floor;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FloorRepository extends JpaRepository<Floor, Long> {
    List<Floor> findAllByProperty_IdOrderByFloorNumberDesc(Long propertyId);
    Optional<Floor> findByIdAndProperty_Id(Long id, Long propertyId);
    boolean existsByProperty_IdAndFloorNumber(Long propertyId, int floorNumber);
}
