package com.pedestrianassistant.Controller.Bot;

import com.pedestrianassistant.Dto.Request.Core.IncidentRequestDto;
import com.pedestrianassistant.Dto.Response.Core.IncidentResponseDto;
import com.pedestrianassistant.Dto.Response.Core.IncidentTypeResponseDto;
import com.pedestrianassistant.Dto.Response.Location.LocationResponseDto;
import com.pedestrianassistant.Exception.InvalidLocationException;
import com.pedestrianassistant.Mapper.Core.IncidentMapper;
import com.pedestrianassistant.Mapper.Core.IncidentTypeMapper;
import com.pedestrianassistant.Mapper.Location.LocationMapper;
import com.pedestrianassistant.Model.Core.Incident;
import com.pedestrianassistant.Model.Location.Location;
import com.pedestrianassistant.Service.Core.IncidentService;
import com.pedestrianassistant.Service.Core.IncidentTypeService;
import com.pedestrianassistant.Service.Location.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bot")
public class BotController {

    private final IncidentService incidentService;
    private final LocationService locationService;
    private final IncidentTypeService incidentTypeService;

    @Autowired
    public BotController(IncidentService incidentService, 
                         LocationService locationService,
                         IncidentTypeService incidentTypeService) {
        this.incidentService = incidentService;
        this.locationService = locationService;
        this.incidentTypeService = incidentTypeService;
    }

    @PostMapping("/incidents")
    public ResponseEntity<?> createIncident(
            @RequestPart("incidentRequestDto") IncidentRequestDto incidentRequestDto,
            @RequestPart("incidentFiles") MultipartFile incidentFiles) {

        if (incidentFiles.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is empty. Please attach a valid file.");
        }

        try {
            Incident savedIncident = incidentService.save(incidentRequestDto, incidentFiles.getInputStream());
            IncidentResponseDto responseDto = IncidentMapper.toDto(savedIncident);

            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while processing the file. Please try again.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred. Please contact support.");
        }
    }

    /**
     * Retrieves the address based on the provided geographic coordinates.
     *
     * @param latitude  the latitude of the location
     * @param longitude the longitude of the location
     * @return a ResponseEntity containing the Location object if successful,
     * or an error message with a 400 Bad Request status if an InvalidLocationException occurs
     */
    @GetMapping("/locations/coordinates")
    public ResponseEntity<?> getLocationFromCoordinates(@RequestParam double latitude, @RequestParam double longitude) {
        try {
            Location location = locationService.getAddressFromCoordinates(latitude, longitude);
            return ResponseEntity.ok(LocationMapper.toDto(location));
        } catch (InvalidLocationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Get all incident types.
     *
     * @return A list of all incident types.
     */
    @GetMapping("/incident-types")
    public ResponseEntity<List<IncidentTypeResponseDto>> getAllIncidentTypes() {
        List<IncidentTypeResponseDto> incidentTypes = incidentTypeService.findAll()
                .stream()
                .map(IncidentTypeMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(incidentTypes);
    }
}