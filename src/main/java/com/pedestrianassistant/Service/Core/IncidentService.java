package com.pedestrianassistant.Service.Core;

import com.pedestrianassistant.Dto.Request.Core.IncidentRequestDto;
import com.pedestrianassistant.Model.Core.Incident;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing incidents.
 */
public interface IncidentService {

    /**
     * Retrieve all incidents.
     *
     * @return A list of all Incident objects.
     */
    List<Incident> findAll();

    /**
     * Retrieve all incidents of a specific type.
     *
     * @param incidentTypeId The ID of the incident type.
     * @return A list of Incident objects matching the specified type.
     */
    List<Incident> findAllByIncidentTypeId(Long incidentTypeId);

    /**
     * Retrieve an incident by its ID.
     *
     * @param id The ID of the incident.
     * @return An Optional containing the Incident object if found, or empty if not found.
     */
    Optional<Incident> findById(Long id);

    /**
     * Save or update an incident based on the provided request data.
     *
     * @param incidentRequestDto The request DTO containing incident data to be saved.
     * @return The saved or updated Incident object.
     */
    Incident save(IncidentRequestDto incidentRequestDto, InputStream incidentPhotosAndVideos);

    /**
     * Delete an incident by its ID.
     *
     * @param id The ID of the incident to delete.
     */
    void deleteById(Long id);
}
