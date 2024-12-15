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
     * Find a location by its latitude and longitude.
     *
     * @param latitude  The latitude of the location.
     * @param longitude The longitude of the location.
     * @return An Optional containing the Location if found, or empty if not found.
     */
    Optional<Location> findByLatitudeAndLongitude(double latitude, double longitude);

    /**
     * Retrieve the address from latitude and longitude using Nominatim API.
     *
     * @param latitude  The latitude of the location.
     * @param longitude The longitude of the location.
     * @return The location.
     */
    Location getAddressFromCoordinates(double latitude, double longitude);

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
