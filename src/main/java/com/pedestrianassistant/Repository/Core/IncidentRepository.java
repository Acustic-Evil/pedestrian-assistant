package com.pedestrianassistant.Repository.Core;

import com.pedestrianassistant.Model.Core.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findAllByIncidentTypeId(Long incidentTypeId);

    List<Incident> findByTitleContainingIgnoreCase(String title);

    List<Incident> findByDescriptionContainingIgnoreCase(String description);

    List<Incident> findByUser_Username(String username);

    @Query("SELECT i FROM Incident i WHERE TO_CHAR(i.createdAt, 'DD-MM-YYYY') = :date")
    List<Incident> findByCreatedAt(@Param("date") String date);

    @Query("SELECT i FROM Incident i WHERE CAST(i.createdAt AS DATE) BETWEEN :startDate AND :endDate")
    List<Incident> findByCreatedAtBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT i FROM Incident i WHERE LOWER(i.location.address) LIKE LOWER(CONCAT('%', :addressPart, '%'))")
    List<Incident> findByLocationAddressContaining(String addressPart);
}

