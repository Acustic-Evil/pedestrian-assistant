package com.pedestrianassistant.Service.Location;

import com.pedestrianassistant.Model.Location.Location;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing locations.
 */
public interface LocationService {

    /**
     * Retrieve all locations.
     *
     * @return A list of all Location objects.
     */
    List<Location> findAll();

    /**
     * Retrieve a location by its ID.
     *
     * @param id The ID of the location.
     * @return An Optional containing the Location object if found, or empty if not found.
     */
    Optional<Location> findById(Long id);

    /**
     * Retrieve a location by its address.
     *
     * @param address The address of the location.
     * @return An Optional containing the Location object if found, or empty if not found.
     */
    Optional<Location> findByAddress(String address);

    /**
     * Save or update a location.
     *
     * @param location The Location object to be saved or updated.
     * @return The saved or updated Location object.
     */
    Location save(Location location);

    /**
     * Delete a location by its ID.
     *
     * @param id The ID of the location to delete.
     */
    void deleteById(Long id);
}
