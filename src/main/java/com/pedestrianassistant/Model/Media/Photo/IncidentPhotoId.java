package com.pedestrianassistant.Model.Media.Photo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentPhotoId implements Serializable {
    private Long incident;
    private Long photo;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IncidentPhotoId that = (IncidentPhotoId) o;
        return Objects.equals(incident, that.incident) && Objects.equals(photo, that.photo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(incident, photo);
    }
}