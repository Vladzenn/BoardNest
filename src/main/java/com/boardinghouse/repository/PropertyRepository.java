package com.boardinghouse.repository;

import com.boardinghouse.model.Property;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findAllByOwner_UsernameOrderByNameAsc(String username);
    Optional<Property> findByIdAndOwner_Username(Long id, String username);
}
