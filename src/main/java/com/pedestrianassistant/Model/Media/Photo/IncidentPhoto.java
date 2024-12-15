package com.pedestrianassistant.Model.Media.Photo;

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
@IdClass(IncidentPhotoId.class)
@Table(name = "incident_photos")
public class IncidentPhoto {

    @Id
    @ManyToOne
    @JoinColumn(name = "id_incident", nullable = false)
    private Incident incident;

    @Id
    @ManyToOne
    @JoinColumn(name = "id_photo", nullable = false)
    private Photo photo;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;
}
