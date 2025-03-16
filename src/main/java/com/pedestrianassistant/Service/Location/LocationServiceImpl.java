package com.pedestrianassistant.Service.Location;

import com.pedestrianassistant.Dto.Request.Location.LocationRequestDto;
import com.pedestrianassistant.Exception.InvalidLocationException;
import com.pedestrianassistant.Model.Location.Location;
import com.pedestrianassistant.Repository.Location.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

@Service
public class LocationServiceImpl implements LocationService {


    private final LocationRepository locationRepository;
    private final RestTemplate restTemplate;

    @Value("${nominatim.api.url}")
    private String nominatimApiUrl;

    @Value("${moscow.latitude.min}")
    private double moscowLatMin;

    @Value("${moscow.latitude.max}")
    private double moscowLatMax;

    @Value("${moscow.longitude.min}")
    private double moscowLonMin;

    @Value("${moscow.longitude.max}")
    private double moscowLonMax;

    @Autowired
    public LocationServiceImpl(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public List<Location> findAll() {
        return locationRepository.findAll();
    }

    @Override
    public Optional<Location> findById(Long id) {
        return locationRepository.findById(id);
    }

    @Override
    public Optional<Location> findByAddress(String address) {
        return locationRepository.findByAddress(address);
    }

    @Override
    public Optional<Location> findByLatitudeAndLongitude(double latitude, double longitude) {
        return locationRepository.findByLatitudeAndLongitude(latitude, longitude);
    }

    @Override
    public Location getAddressFromCoordinates(double latitude, double longitude) {
        // Build the URL for the Nominatim API request
        String url = UriComponentsBuilder.fromHttpUrl(nominatimApiUrl)
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .toUriString();

        NominatimResponse response = restTemplate.getForObject(url, NominatimResponse.class);

        // Check if the response is valid
        if (response == null || response.getDisplayName() == null) {
            throw new InvalidLocationException("Failed to retrieve the address from Nominatim API.");
        }

        String address = response.getDisplayName();

        // Verify if the location is within Moscow boundaries
        if (!isWithinMoscowBounds(latitude, longitude)) {
            throw new InvalidLocationException("Coordinates are outside the boundaries of Moscow.");
        }

        // Check if this address already exists in the database
        Optional<Location> existingLocation = locationRepository.findByAddress(address);

        if (existingLocation.isPresent()) {
            return existingLocation.get(); // Return the existing record if found
        }

        // If the address is not found, create a new location record
        Location newLocation = new Location();
        newLocation.setAddress(address);
        newLocation.setLatitude(latitude);
        newLocation.setLongitude(longitude);

        return locationRepository.save(newLocation);
    }

    private boolean isWithinMoscowBounds(double latitude, double longitude) {
        return latitude >= moscowLatMin && latitude <= moscowLatMax &&
                longitude >= moscowLonMin && longitude <= moscowLonMax;
    }

    @Override
    public Location save(LocationRequestDto locationRequestDto) {
        Location location = new Location();

        location.setAddress(locationRequestDto.getAddress());
        location.setLatitude(locationRequestDto.getLatitude());
        location.setLongitude(locationRequestDto.getLongitude());

        return locationRepository.save(location);
    }

    @Override
    public void deleteById(Long id) {
        if (locationRepository.existsById(id)) {
            locationRepository.deleteById(id);
        } else {
            throw new RuntimeException("Location with id " + id + " not found");
        }
    }
}
