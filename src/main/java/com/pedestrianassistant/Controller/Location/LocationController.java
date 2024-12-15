package com.pedestrianassistant.Controller.Location;

import com.pedestrianassistant.Dto.Response.Location.LocationResponseDto;
import com.pedestrianassistant.Mapper.Location.LocationMapper;
import com.pedestrianassistant.Model.Location.Location;
import com.pedestrianassistant.Service.Location.LocationService;
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
     * Get a location by address.
     *
     * @param address The address of the location.
     * @return The location with the specified address, if found.
     */
    @GetMapping("/address")
    public ResponseEntity<LocationResponseDto> getLocationByAddress(@RequestParam String address) {
        Optional<Location> location = locationService.findByAddress(address);
        return location.map(value -> ResponseEntity.ok(LocationMapper.toDto(value)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Create or update a location.
     *
     * @param location The location to be saved or updated.
     * @return The created or updated location.
     */
    @PostMapping
    public ResponseEntity<LocationResponseDto> createOrUpdateLocation(@RequestBody Location location) {
        Location savedLocation = locationService.save(location);
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
