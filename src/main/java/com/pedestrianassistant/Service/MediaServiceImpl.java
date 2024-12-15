package com.pedestrianassistant.Service;

import com.pedestrianassistant.Model.Media.Photo.Photo;
import com.pedestrianassistant.Model.Media.Video.Video;
import com.pedestrianassistant.Repository.PhotoRepository;
import com.pedestrianassistant.Repository.VideoRepository;
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
import java.util.stream.Stream;

@Service
public class MediaServiceImpl implements MediaService {

    private final PhotoRepository photoRepository;
    private final VideoRepository videoRepository;
    private final MediaStorage mediaStorage;

    @Autowired
    public MediaServiceImpl(PhotoRepository photoRepository, VideoRepository videoRepository, MediaStorage mediaStorage) {
        this.photoRepository = photoRepository;
        this.videoRepository = videoRepository;
        this.mediaStorage = mediaStorage;
    }

    @Override
    public void processAndStoreMedia(InputStream zipInputStream) {
        // Extract ZIP to a unique directory
        Path uniqueDirPath = mediaStorage.extractZipToUniqueDir(zipInputStream);

        try (Stream<Path> files = Files.walk(uniqueDirPath)) {
            Tika tika = new Tika();

            // Process each file in the extracted directory
            files.filter(Files::isRegularFile).forEach(file -> {
                try {
                    String mimeType = tika.detect(file);

                    if (mimeType.startsWith("video")) {
                        processVideoFile(file);
                    } else if (mimeType.startsWith("image")) {
                        processImageFile(file);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Error processing file: " + file, e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Error scanning directory: " + uniqueDirPath, e);
        }
    }

    /**
     * Process a video file and store its metadata in the database.
     *
     * @param videoFile Path to the video file.
     */
    private void processVideoFile(Path videoFile) {
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
        } catch (IOException e) {
            throw new RuntimeException("Error processing video file: " + videoFile, e);
        }
    }

    /**
     * Process an image file and store its metadata in the database.
     *
     * @param imageFile Path to the image file.
     */
    private void processImageFile(Path imageFile) {
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
