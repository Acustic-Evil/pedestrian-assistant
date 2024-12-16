package com.pedestrianassistant.Controller.Location;

import com.pedestrianassistant.Dto.Request.Location.LocationRequestDto;
import com.pedestrianassistant.Dto.Response.Location.LocationResponseDto;
import com.pedestrianassistant.Exception.InvalidLocationException;
import com.pedestrianassistant.Mapper.Location.LocationMapper;
import com.pedestrianassistant.Model.Location.Location;
import com.pedestrianassistant.Service.Location.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    @Autowired
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * Get all locations.
     *
     * @return A list of all locations.
     */
    @GetMapping
    public ResponseEntity<List<LocationResponseDto>> getAllLocations() {
        List<LocationResponseDto> locations = locationService.findAll()
                .stream()
                .map(LocationMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(locations);
    }

    /**
     * Get a location by ID.
     *
     * @param id The ID of the location.
     * @return The location with the specified ID, if found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LocationResponseDto> getLocationById(@PathVariable Long id) {
        Optional<Location> location = locationService.findById(id);
        return location.map(value -> ResponseEntity.ok(LocationMapper.toDto(value)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Retrieves the address based on the provided geographic coordinates.
     *
     * @param latitude  the latitude of the location
     * @param longitude the longitude of the location
     * @return a ResponseEntity containing the Location object if successful,
     * or an error message with a 400 Bad Request status if an InvalidLocationException occurs
     */
    @GetMapping("/coordinates")
    public ResponseEntity<?> getLocationFromCoordinates(@RequestParam double latitude, @RequestParam double longitude) {
        try {
            Location location = locationService.getAddressFromCoordinates(latitude, longitude);
            return ResponseEntity.ok(LocationMapper.toDto(location));
        } catch (InvalidLocationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Create or update a location.
     *
     * @param locationRequestDto The location to be created.
     * @return The created or updated location.
     */
    @PostMapping
    public ResponseEntity<LocationResponseDto> createLocation(@RequestBody LocationRequestDto locationRequestDto) {
        Location savedLocation = locationService.save(locationRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(LocationMapper.toDto(savedLocation));
    }

    /**
     * Delete a location by ID.
     *
     * @param id The ID of the location to delete.
     * @return A response indicating the result of the operation.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        locationService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
