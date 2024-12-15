package com.pedestrianassistant.Model.Media;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "videos")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", columnDefinition = "TEXT", nullable = false)
    private String fileName;

    @Column(name = "file_path", columnDefinition = "TEXT", nullable = false)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Float fileSize;

    @Column(name = "duration_in_seconds")
    private Integer durationInSeconds;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
