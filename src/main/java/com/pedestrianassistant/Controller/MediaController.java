package com.pedestrianassistant.Controller;

import com.pedestrianassistant.Service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaService mediaService;

    @Autowired
    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    /**
     * Endpoint to upload and process a media ZIP file.
     *
     * @param file The ZIP file uploaded by the user.
     * @return ResponseEntity with the processing status.
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadMedia(@RequestParam("zip-file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("The file cannot be empty");
        }

        try {
            mediaService.processAndStoreMedia(file.getInputStream());
            return ResponseEntity.ok("Media files have been successfully processed and saved");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error reading the file: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing the file: " + e.getMessage());
        }
    }
}
