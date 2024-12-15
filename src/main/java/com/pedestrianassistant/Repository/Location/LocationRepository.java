package com.pedestrianassistant.Repository.Location;

import com.pedestrianassistant.Model.Location.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByAddress(String address);
}
