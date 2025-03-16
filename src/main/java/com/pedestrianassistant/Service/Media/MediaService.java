package com.pedestrianassistant.Service.Media;

import com.pedestrianassistant.Model.Media.Photo.IncidentPhoto;
import com.pedestrianassistant.Model.Media.Video.IncidentVideo;
import com.pedestrianassistant.Model.Core.Incident;
import com.pedestrianassistant.Model.Media.Photo.Photo;
import com.pedestrianassistant.Model.Media.Video.Video;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface MediaService {

    /**
     * Process and store media files from the provided ZIP archive.
     *
     * @param zipInputStream InputStream of the ZIP archive containing media files.
     * @return MediaServiceResult containing lists of created photos and videos.
     */
    MediaServiceResult processAndStoreMedia(InputStream zipInputStream);

    /**
     * Get binary data (byte[]) of all photos and videos associated with a specific incident.
     *
     * @param incidentId ID of the incident.
     * @return Map where keys are filenames and values are byte arrays of the media files.
     */
    Map<String, byte[]> getIncidentMedia(Long incidentId);

    /**
     * Get all photos associated with a specific incident.
     *
     * @param incidentId ID of the incident.
     * @return List of Photo objects associated with the incident.
     */
    List<Photo> getAllPhotosByIncidentId(Long incidentId);

    /**
     * Get all videos associated with a specific incident.
     *
     * @param incidentId ID of the incident.
     * @return List of Video objects associated with the incident.
     */
    List<Video> getAllVideosByIncidentId(Long incidentId);

    /**
     * Create a new IncidentPhoto record.
     *
     * @param incident Incident associated with the photo.
     * @param photo Photo to associate with the incident.
     * @return The created IncidentPhoto object.
     */
    IncidentPhoto createIncidentPhoto(Incident incident, Photo photo);

    /**
     * Create a new IncidentVideo record.
     *
     * @param incident Incident associated with the video.
     * @param video Video to associate with the incident.
     * @return The created IncidentVideo object.
     */
    IncidentVideo createIncidentVideo(Incident incident, Video video);
}
