package com.pedestrianassistant.Repository.Media.Video;

import com.pedestrianassistant.Model.Media.Video.IncidentVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncidentVideoRepository extends JpaRepository<IncidentVideo, Long> {
}
