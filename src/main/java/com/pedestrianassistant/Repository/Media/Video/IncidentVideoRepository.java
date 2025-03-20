package com.pedestrianassistant.Repository.Media.Video;

import com.pedestrianassistant.Model.Media.Video.IncidentVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentVideoRepository extends JpaRepository<IncidentVideo, Long> {
    List<IncidentVideo> findByIncidentId(Long incidentId);
}
