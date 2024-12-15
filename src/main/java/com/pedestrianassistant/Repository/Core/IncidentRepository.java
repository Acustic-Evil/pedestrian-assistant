package com.pedestrianassistant.Repository.Core;

import com.pedestrianassistant.Model.Core.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findAllByIncidentTypeId(Long incidentTypeId);

    List<Incident> findByTitleContainingIgnoreCase(String title);

    List<Incident> findByDescriptionContainingIgnoreCase(String description);

    List<Incident> findByUser_Username(String username);

    @Query("SELECT i FROM Incident i WHERE FUNCTION('DATE_FORMAT', i.createdAt, '%d-%m-%Y') = :date")
    List<Incident> findByCreatedAt(String date);

    @Query("SELECT i FROM Incident i WHERE FUNCTION('DATE_FORMAT', i.createdAt, '%d-%m-%Y') BETWEEN :startDate AND :endDate")
    List<Incident> findByCreatedAtBetween(String startDate, String endDate);

    @Query("SELECT i FROM Incident i WHERE LOWER(i.location.address) LIKE LOWER(CONCAT('%', :addressPart, '%'))")
    List<Incident> findByLocationAddressContaining(String addressPart);
}

