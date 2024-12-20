package com.pedestrianassistant.Repository.Media.Video;

import com.pedestrianassistant.Model.Media.Video.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
}
