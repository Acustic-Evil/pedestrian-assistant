package com.pedestrianassistant.Controller.Media;

import com.pedestrianassistant.Service.Media.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/media")
public class MediaController {

    private final MediaService mediaService;

    @Autowired
    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    /**
     * Fetch all media file URLs (photos & videos) related to an incident.
     *
     * @param incidentId The ID of the incident.
     * @return List of media URLs.
     */
    @GetMapping("/incident/{incidentId}")
    public ResponseEntity<List<String>> getIncidentMediaUrls(@PathVariable Long incidentId) {
        List<String> mediaUrls = mediaService.getIncidentMediaUrls(incidentId);

        if (mediaUrls.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(mediaUrls);
    }
}
