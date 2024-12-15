package com.pedestrianassistant.Model.Media.IncidentVideo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentVideoId implements Serializable {
    private Long incident;
    private Long video;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IncidentVideoId that = (IncidentVideoId) o;
        return Objects.equals(incident, that.incident) && Objects.equals(video, that.video);
    }

    @Override
    public int hashCode() {
        return Objects.hash(incident, video);
    }
}
