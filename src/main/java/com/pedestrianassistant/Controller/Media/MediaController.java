package com.pedestrianassistant.Controller.Media;

import com.pedestrianassistant.Service.Media.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.ByteArrayResource;

import java.util.Map;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaService mediaService;

    @Autowired
    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    /**
     * Get all media files (photos & videos) related to an incident in binary format.
     *
     * @param incidentId The ID of the incident.
     * @return A ResponseEntity containing the media files as a multipart response.
     */
    @GetMapping("/incident/{incidentId}")
    public ResponseEntity<MultiValueMap<String, HttpEntity<?>>> getIncidentMedia(@PathVariable Long incidentId) {
        Map<String, byte[]> mediaFiles = mediaService.getIncidentMedia(incidentId);

        if (mediaFiles.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        for (Map.Entry<String, byte[]> entry : mediaFiles.entrySet()) {
            ByteArrayResource resource = new ByteArrayResource(entry.getValue()) {
                @Override
                public String getFilename() {
                    return entry.getKey();
                }
            };

            builder.part(entry.getKey(), resource)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + entry.getKey() + "\"");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.MULTIPART_MIXED)
                .body(builder.build());
    }

}
