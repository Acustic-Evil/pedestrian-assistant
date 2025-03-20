package com.pedestrianassistant.Repository.Media.Photo;

import com.pedestrianassistant.Model.Media.Photo.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {

    @Query("SELECT p FROM Photo p JOIN IncidentPhoto ip ON ip.photo.id = p.id WHERE ip.incident.id = :incidentId")
    List<Photo> findPhotosByIncidentId(@Param("incidentId") Long incidentId);

}
