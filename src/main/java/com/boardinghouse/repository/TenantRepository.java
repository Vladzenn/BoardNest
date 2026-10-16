package com.boardinghouse.repository;

import com.boardinghouse.model.Tenant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    List<Tenant> findAllByProperty_IdOrderByFullNameAsc(Long propertyId);
    Optional<Tenant> findByIdAndProperty_Id(Long id, Long propertyId);
}
