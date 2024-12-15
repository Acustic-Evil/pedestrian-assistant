package com.pedestrianassistant.Controller.Core;

import com.pedestrianassistant.Dto.Response.Core.IncidentTypeResponseDto;
import com.pedestrianassistant.Mapper.Core.IncidentTypeMapper;
import com.pedestrianassistant.Model.Core.IncidentType;
import com.pedestrianassistant.Service.Core.IncidentTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/incident-types")
public class IncidentTypeController {

    private final IncidentTypeService incidentTypeService;

    public IncidentTypeController(IncidentTypeService incidentTypeService) {
        this.incidentTypeService = incidentTypeService;
    }

    /**
     * Get all incident types.
     *
     * @return A list of all incident types.
     */
    @GetMapping
    public ResponseEntity<List<IncidentTypeResponseDto>> getAllIncidentTypes() {
        List<IncidentTypeResponseDto> incidentTypes = incidentTypeService.findAll()
                .stream()
                .map(IncidentTypeMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(incidentTypes);
    }

    /**
     * Get an incident type by ID.
     *
     * @param id The ID of the incident type.
     * @return The incident type with the specified ID, if found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<IncidentTypeResponseDto> getIncidentTypeById(@PathVariable Long id) {
        Optional<IncidentType> incidentType = incidentTypeService.findById(id);
        return incidentType.map(type -> ResponseEntity.ok(IncidentTypeMapper.toDto(type)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Create or update an incident type.
     *
     * @param incidentType The incident type to be saved or updated.
     * @return The created or updated incident type.
     */
    @PostMapping
    public ResponseEntity<IncidentTypeResponseDto> createOrUpdateIncidentType(@RequestBody IncidentType incidentType) {
        IncidentType savedType = incidentTypeService.save(incidentType);
        return ResponseEntity.status(HttpStatus.CREATED).body(IncidentTypeMapper.toDto(savedType));
    }

    /**
     * Delete an incident type by ID.
     *
     * @param id The ID of the incident type to delete.
     * @return A response indicating the result of the operation.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncidentType(@PathVariable Long id) {
        incidentTypeService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
