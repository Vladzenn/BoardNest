package com.boardinghouse.repository;

import com.boardinghouse.model.Occupancy;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OccupancyRepository extends JpaRepository<Occupancy, Long> {
    Optional<Occupancy> findByTenant_Id(Long tenantId);
    List<Occupancy> findAllByRoom_Id(Long roomId);
    long countByRoom_Id(Long roomId);
}
