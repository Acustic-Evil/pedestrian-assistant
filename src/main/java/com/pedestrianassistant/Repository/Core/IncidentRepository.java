package com.pedestrianassistant.Repository.Core;

import com.pedestrianassistant.Model.Core.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findAllByIncidentTypeId(Long incidentTypeId);
}
