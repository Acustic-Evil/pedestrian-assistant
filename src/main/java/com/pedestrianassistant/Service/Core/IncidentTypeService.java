package com.pedestrianassistant.Service.Core;

import com.pedestrianassistant.Dto.Request.Core.IncidentTypeRequestDto;
import com.pedestrianassistant.Model.Core.IncidentType;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing incident types.
 */
public interface IncidentTypeService {

    /**
     * Retrieve all incident types.
     *
     * @return A list of all IncidentType objects.
     */
    List<IncidentType> findAll();

    /**
     * Retrieve an incident type by its ID.
     *
     * @param id The ID of the incident type.
     * @return An Optional containing the IncidentType object if found, or empty if not found.
     */
    Optional<IncidentType> findById(Long id);

    /**
     * Save or update an incident type.
     *
     * @param incidentTypeRequestDto The IncidentType object to be saved or updated.
     * @return The saved or updated IncidentType object.
     */
    IncidentType save(IncidentTypeRequestDto incidentTypeRequestDto);

    /**
     * Delete an incident type by its ID.
     *
     * @param id The ID of the incident type to delete.
     */
    void deleteById(Long id);
}
