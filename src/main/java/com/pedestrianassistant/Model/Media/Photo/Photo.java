package com.pedestrianassistant.Model.Media.Photo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "photos")
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", columnDefinition = "TEXT", nullable = false)
    private String fileName;

    @Column(name = "file_path", columnDefinition = "TEXT", nullable = false)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Float fileSize;

    @Column(name = "resolution_width")
    private Integer resolutionWidth;

    @Column(name = "resolution_height")
    private Integer resolutionHeight;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
