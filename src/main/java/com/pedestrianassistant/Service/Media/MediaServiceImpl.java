package com.pedestrianassistant.Service.Media;

import com.pedestrianassistant.Model.Core.Incident;
import com.pedestrianassistant.Model.Media.Photo.IncidentPhoto;
import com.pedestrianassistant.Model.Media.Photo.Photo;
import com.pedestrianassistant.Model.Media.Video.IncidentVideo;
import com.pedestrianassistant.Model.Media.Video.Video;
import com.pedestrianassistant.Repository.Media.Photo.IncidentPhotoRepository;
import com.pedestrianassistant.Repository.Media.Photo.PhotoRepository;
import com.pedestrianassistant.Repository.Media.Video.IncidentVideoRepository;
import com.pedestrianassistant.Repository.Media.Video.VideoRepository;
import com.pedestrianassistant.Util.Media.MediaStorage;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MediaServiceImpl implements MediaService {

    private final PhotoRepository photoRepository;
    private final IncidentPhotoRepository incidentPhotoRepository;
    private final VideoRepository videoRepository;
    private final IncidentVideoRepository incidentVideoRepository;
    private final MediaStorage mediaStorage;

    @Autowired
    public MediaServiceImpl(
            PhotoRepository photoRepository,
            IncidentPhotoRepository incidentPhotoRepository,
            VideoRepository videoRepository,
            IncidentVideoRepository incidentVideoRepository,
            MediaStorage mediaStorage) {
        this.photoRepository = photoRepository;
        this.incidentPhotoRepository = incidentPhotoRepository;
        this.videoRepository = videoRepository;
        this.incidentVideoRepository = incidentVideoRepository;
        this.mediaStorage = mediaStorage;
    }

    @Override
    public MediaServiceResult processAndStoreMedia(InputStream zipInputStream) {
        // Initialize lists to store created photo and video objects
        List<Photo> photos = new ArrayList<>();
        List<Video> videos = new ArrayList<>();

        // Extract ZIP to a unique directory
        Path uniqueDirPath = mediaStorage.extractZipToUniqueDir(zipInputStream);

        try (Stream<Path> files = Files.walk(uniqueDirPath)) {
            Tika tika = new Tika();

            // Process each file in the extracted directory
            files.filter(Files::isRegularFile).forEach(file -> {
                try {
                    String mimeType = tika.detect(file);

                    if (mimeType.startsWith("video")) {
                        videos.add(processVideoFile(file));
                    } else if (mimeType.startsWith("image")) {
                        photos.add(processImageFile(file));
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Error processing file: " + file, e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Error scanning directory: " + uniqueDirPath, e);
        }

        // Return the result object
        return new MediaServiceResult(photos, videos);
    }

    @Override
    public List<Photo> getAllPhotosByIncidentId(Long incidentId) {
        return incidentPhotoRepository.findAll().stream()
                .filter(incidentPhoto -> incidentPhoto.getIncident().getId().equals(incidentId))
                .map(IncidentPhoto::getPhoto)
                .collect(Collectors.toList());
    }

    @Override
    public List<Video> getAllVideosByIncidentId(Long incidentId) {
        return incidentVideoRepository.findAll().stream()
                .filter(incidentVideo -> incidentVideo.getIncident().getId().equals(incidentId))
                .map(IncidentVideo::getVideo)
                .collect(Collectors.toList());
    }

    @Override
    public IncidentPhoto createIncidentPhoto(Incident incident, Photo photo) {
        IncidentPhoto incidentPhoto = new IncidentPhoto();
        incidentPhoto.setIncident(incident);
        incidentPhoto.setPhoto(photo);
        incidentPhoto.setAddedAt(LocalDateTime.now());
        return incidentPhotoRepository.save(incidentPhoto);
    }

    @Override
    public IncidentVideo createIncidentVideo(Incident incident, Video video) {
        IncidentVideo incidentVideo = new IncidentVideo();
        incidentVideo.setIncident(incident);
        incidentVideo.setVideo(video);
        incidentVideo.setAddedAt(LocalDateTime.now());
        return incidentVideoRepository.save(incidentVideo);
    }

    /**
     * Process a video file and store its metadata in the database.
     *
     * @param videoFile Path to the video file.
     * @return The created Video object.
     */
    private Video processVideoFile(Path videoFile) {
        try {
            float fileSize = (float) Files.size(videoFile) / (1024 * 1024); // Size in MB
            int durationInSeconds = getVideoDuration(videoFile); // Implement this method to get video duration

            Video video = new Video();
            video.setFileName(videoFile.getFileName().toString());
            video.setFilePath(videoFile.getParent().toAbsolutePath().toString());
            video.setFileSize(fileSize);
            video.setDurationInSeconds(durationInSeconds);
            video.setCreatedAt(LocalDateTime.now());

            videoRepository.save(video);
            return video;
        } catch (IOException e) {
            throw new RuntimeException("Error processing video file: " + videoFile, e);
        }
    }

    /**
     * Process an image file and store its metadata in the database.
     *
     * @param imageFile Path to the image file.
     * @return The created Photo object.
     */
    private Photo processImageFile(Path imageFile) {
        try {
            BufferedImage image = ImageIO.read(imageFile.toFile());
            if (image == null) {
                throw new RuntimeException("Unable to read image: " + imageFile);
            }

            float fileSize = (float) Files.size(imageFile) / (1024 * 1024); // Size in MB
            int width = image.getWidth();
            int height = image.getHeight();

            Photo photo = new Photo();
            photo.setFileName(imageFile.getFileName().toString());
            photo.setFilePath(imageFile.getParent().toAbsolutePath().toString());
            photo.setFileSize(fileSize);
            photo.setResolutionWidth(width);
            photo.setResolutionHeight(height);
            photo.setCreatedAt(LocalDateTime.now());

            photoRepository.save(photo);
            return photo;
        } catch (IOException e) {
            throw new RuntimeException("Error processing image file: " + imageFile, e);
        }
    }

    /**
     * Placeholder method to get the duration of a video file.
     * Implement this method using FFmpeg or another library.
     *
     * @param videoFile Path to the video file.
     * @return Duration of the video in seconds.
     */
    private int getVideoDuration(Path videoFile) {
        // Use a library like FFmpeg to get video metadata
        return 120; // Placeholder value for testing
    }
}
