package com.pedestrianassistant.Repository.Media.Photo;

import com.pedestrianassistant.Model.Media.Photo.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
}
