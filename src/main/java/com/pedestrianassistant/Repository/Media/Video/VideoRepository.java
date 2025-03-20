package com.pedestrianassistant.Repository.Media.Video;

import com.pedestrianassistant.Model.Media.Video.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    @Query("SELECT v FROM Video v JOIN IncidentVideo iv ON iv.video.id = v.id WHERE iv.incident.id = :incidentId")
    List<Video> findVideosByIncidentId(@Param("incidentId") Long incidentId);
}
