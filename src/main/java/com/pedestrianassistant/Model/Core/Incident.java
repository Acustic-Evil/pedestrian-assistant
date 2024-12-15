package com.pedestrianassistant.Model.Core;

import com.pedestrianassistant.Model.Location.Location;
import com.pedestrianassistant.Model.Media.IncidentPhoto.IncidentPhoto;
import com.pedestrianassistant.Model.Media.IncidentVideo.IncidentVideo;
import com.pedestrianassistant.Model.User.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "incidents")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", columnDefinition = "TEXT", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Location location;

    @ManyToOne
    @JoinColumn(name = "incident_type")
    private IncidentType incidentType;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IncidentPhoto> incidentPhotos;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IncidentVideo> incidentVideos;
}
