package com.pedestrianassistant.Repository.Core;

import com.pedestrianassistant.Model.Core.IncidentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncidentTypeRepository extends JpaRepository<IncidentType, Long> {
}
