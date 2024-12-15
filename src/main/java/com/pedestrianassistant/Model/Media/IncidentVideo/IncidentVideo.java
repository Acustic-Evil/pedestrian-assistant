package com.pedestrianassistant.Model.Media.IncidentVideo;

import com.pedestrianassistant.Model.Core.Incident;
import com.pedestrianassistant.Model.Media.Video;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "incident_videos")
public class IncidentVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_incident", nullable = false)
    private Incident incident;

    @ManyToOne
    @JoinColumn(name = "id_video", nullable = false)
    private Video video;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;
}
