package com.pedestrianassistant.Controller.Core;

import com.pedestrianassistant.Dto.Request.Core.IncidentRequestDto;
import com.pedestrianassistant.Dto.Response.Core.IncidentResponseDto;
import com.pedestrianassistant.Mapper.Core.IncidentMapper;
import com.pedestrianassistant.Model.Core.Incident;
import com.pedestrianassistant.Service.Core.IncidentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    /**
     * Get all incidents.
     *
     * @return A list of all incidents.
     */
    @GetMapping
    public ResponseEntity<List<IncidentResponseDto>> getAllIncidents() {
        List<IncidentResponseDto> incidents = incidentService.findAll()
                .stream()
                .map(IncidentMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(incidents);
    }

    /**
     * Get incidents by type ID.
     *
     * @param typeId The ID of the incident type.
     * @return A list of incidents of the specified type.
     */
    @GetMapping("/type/{typeId}")
    public ResponseEntity<List<IncidentResponseDto>> getIncidentsByTypeId(@PathVariable Long typeId) {
        List<IncidentResponseDto> incidents = incidentService.findAllByIncidentTypeId(typeId)
                .stream()
                .map(IncidentMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(incidents);
    }

    /**
     * Get an incident by ID.
     *
     * @param id The ID of the incident.
     * @return The incident with the specified ID, if found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponseDto> getIncidentById(@PathVariable Long id) {
        Optional<Incident> incident = incidentService.findById(id);
        return incident.map(value -> ResponseEntity.ok(IncidentMapper.toDto(value)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Create an incident.
     *
     * @param incidentRequestDto The request data for the incident.
     * @return The created incident.
     */
    @PostMapping
    public ResponseEntity<Object> createIncident(
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
     * Delete an incident by ID.
     *
     * @param id The ID of the incident to delete.
     * @return A response indicating the result of the operation.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncident(@PathVariable Long id) {
        incidentService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
