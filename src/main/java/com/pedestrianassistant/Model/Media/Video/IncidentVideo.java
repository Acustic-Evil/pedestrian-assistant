package com.pedestrianassistant.Model.Media.Video;

import com.pedestrianassistant.Model.Core.Incident;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(IncidentVideoId.class)
@Table(name = "incident_videos")
public class IncidentVideo {

    @Id
    @ManyToOne
    @JoinColumn(name = "id_incident", nullable = false)
    private Incident incident;

    @Id
    @ManyToOne
    @JoinColumn(name = "id_video", nullable = false)
    private Video video;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;
}
