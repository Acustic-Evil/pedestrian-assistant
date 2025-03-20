package com.pedestrianassistant.Repository.Media.Photo;

import com.pedestrianassistant.Model.Media.Photo.IncidentPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentPhotoRepository extends JpaRepository<IncidentPhoto, Long> {
    List<IncidentPhoto> findByIncidentId(Long incidentId);
}
