package com.pedestrianassistant.Controller.Media;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/user/media")
public class MediaDownloadController {

    @Value("${media.storage.path}")
    private String mediaStoragePath;

    /**
     * Serves media files (images/videos) from storage.
     *
     * @param dateFolder The folder name (e.g., 2025_03_18).
     * @param reportFolder The specific report folder (e.g., report_1_2025_03_18).
     * @param filename The filename of the media.
     * @return The requested media file.
     */
    @GetMapping("/file/{dateFolder}/{reportFolder}/{filename}")
    public ResponseEntity<Resource> downloadMedia(
            @PathVariable String dateFolder,
            @PathVariable String reportFolder,
            @PathVariable String filename) {
        try {
            // Construct full file path
            Path filePath = Paths.get(mediaStoragePath, dateFolder, reportFolder, filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(getContentType(filename))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Determines the appropriate content type for a file.
     */
    private MediaType getContentType(String filename) {
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        } else if (filename.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        } else if (filename.endsWith(".mp4")) {
            return MediaType.valueOf("video/mp4");
        } else if (filename.endsWith(".mov")) {
            return MediaType.valueOf("video/quicktime");
        }
        return MediaType.APPLICATION_OCTET_STREAM; // Default for unknown types
    }
}
