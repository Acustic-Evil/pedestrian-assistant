package com.pedestrianassistant.Service.Core;

import com.pedestrianassistant.Dto.Request.Core.IncidentRequestDto;
import com.pedestrianassistant.Model.Core.Incident;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
     * Retrieve all incidents matching the specified title.
     * The search is case-insensitive and supports partial matches.
     *
     * @param title The title or part of the title to search for.
     * @return A list of Incident objects with matching titles.
     */
    List<Incident> findByTitle(String title);

    /**
     * Retrieve all incidents matching the specified description.
     * The search is case-insensitive and supports partial matches.
     *
     * @param description The description or part of the description to search for.
     * @return A list of Incident objects with matching descriptions.
     */
    List<Incident> findByDescription(String description);

    /**
     * Retrieve all incidents associated with a specific username.
     *
     * @param username The username of the user to search for.
     * @return A list of Incident objects created by the specified user.
     */
    List<Incident> findByUsername(String username);

    /**
     * Retrieve all incidents created on a specific date.
     * The date must be provided in the format DD-MM-YYYY.
     *
     * @param date The date of creation in DD-MM-YYYY format.
     * @return A list of Incident objects created on the specified date.
     */
    List<Incident> findByCreatedAt(String date);

    /**
     * Retrieve all incidents created within a specific date range.
     * Both start and end dates must be provided in the format DD-MM-YYYY.
     *
     * @param startDate The start date of the range in DD-MM-YYYY format.
     * @param endDate   The end date of the range in DD-MM-YYYY format.
     * @return A list of Incident objects created within the specified date range.
     */
    List<Incident> findByCreatedAtBetween(String startDate, String endDate);

    /**
     * Save or update an incident based on the provided request data.
     * This method also handles the processing and storage of associated media files.
     *
     * @param incidentRequestDto      The request DTO containing incident data to be saved.
     * @param incidentPhotosAndVideos An InputStream containing the associated photos and videos.
     * @return The saved or updated Incident object.
     */
    Incident save(IncidentRequestDto incidentRequestDto, InputStream incidentPhotosAndVideos);

    /**
     * Retrieve incidents by location address containing the specified part.
     * The search is case-insensitive and matches addresses that contain the provided substring.
     *
     * @param addressPart The substring of the address to search for.
     * @return A list of Incident objects with matching addresses.
     */
    List<Incident> findByLocationAddressContaining(String addressPart);

    /**
     * Delete an incident by its ID.
     *
     * @param id The ID of the incident to delete.
     */
    void deleteById(Long id);

    /**
     *  Finds all incidents by any field incident has.
     *
     * @param title
     * @param description
     * @param userId
     * @param locationId
     * @param incidentTypeId
     * @param startDate
     * @param endDate
     * @return
     */
    List<Incident> findByFilters(String title, String description, Long userId, Long locationId, String address, Long incidentTypeId,
                                 LocalDateTime startDate, LocalDateTime endDate);
}

