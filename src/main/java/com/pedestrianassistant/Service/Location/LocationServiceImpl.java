package com.pedestrianassistant.Service.Location;

import com.pedestrianassistant.Dto.Request.Location.LocationRequestDto;
import com.pedestrianassistant.Model.Location.Location;
import com.pedestrianassistant.Repository.Location.LocationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;

    public LocationServiceImpl(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
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
    public Location save(Location location) {
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
