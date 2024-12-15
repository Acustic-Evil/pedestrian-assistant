package com.pedestrianassistant.Service.Core;

import com.pedestrianassistant.Dto.Request.Core.IncidentRequestDto;
import com.pedestrianassistant.Dto.Request.Location.LocationRequestDto;
import com.pedestrianassistant.Model.Core.Incident;
import com.pedestrianassistant.Model.Core.IncidentType;
import com.pedestrianassistant.Model.Location.Location;
import com.pedestrianassistant.Model.Media.Photo.Photo;
import com.pedestrianassistant.Model.Media.Video.Video;
import com.pedestrianassistant.Model.User.Role;
import com.pedestrianassistant.Model.User.User;
import com.pedestrianassistant.Repository.Core.IncidentRepository;
import com.pedestrianassistant.Service.Location.LocationService;
import com.pedestrianassistant.Service.Media.MediaServiceResult;
import com.pedestrianassistant.Service.Media.MediaService;
import com.pedestrianassistant.Service.User.RoleService;
import com.pedestrianassistant.Service.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final UserService userService;
    private final RoleService roleService;
    private final LocationService locationService;
    private final IncidentTypeService incidentTypeService;
    private final MediaService mediaService;

    // ctor
    @Autowired
    public IncidentServiceImpl(
            IncidentRepository incidentRepository,
            UserService userService,
            RoleService roleService,
            LocationService locationService,
            IncidentTypeService incidentTypeService,
            MediaService mediaService) {
        this.incidentRepository = incidentRepository;
        this.userService = userService;
        this.roleService = roleService;
        this.locationService = locationService;
        this.incidentTypeService = incidentTypeService;
        this.mediaService = mediaService;
    }

    @Override
    public List<Incident> findAll() {
        return incidentRepository.findAll();
    }

    @Override
    public List<Incident> findAllByIncidentTypeId(Long incidentTypeId) {
        return incidentRepository.findAllByIncidentTypeId(incidentTypeId);
    }

    @Override
    public Optional<Incident> findById(Long id) {
        return incidentRepository.findById(id);
    }

    @Override
    public Incident save(IncidentRequestDto incidentRequestDto, InputStream incidentPhotosAndVideos) {

        Incident incident = new Incident();

        User user = userService.findByUsername(incidentRequestDto.getUsername())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(incidentRequestDto.getUsername());

                    Role defaultRole = roleService.findById(1L)
                            .orElseThrow(() -> new RuntimeException("Default role not found"));

                    newUser.setRole(defaultRole);
                    return userService.save(newUser);
                });

        LocationRequestDto locationRequestDto = incidentRequestDto.getLocationRequestDto();

        Location location = locationService.findByAddress(locationRequestDto.getAddress())
                .orElseGet(() -> {
                    Location newLocation = new Location();
                    newLocation.setAddress(locationRequestDto.getAddress());
                    newLocation.setLatitude(locationRequestDto.getLatitude());
                    newLocation.setLongitude(locationRequestDto.getLongitude());
                    return locationService.save(newLocation);
                });

        IncidentType incidentType = incidentTypeService.findById(incidentRequestDto.getIncidentTypeId())
                .orElseThrow(NullPointerException::new);

        MediaServiceResult mediaServiceResult =
                mediaService.processAndStoreMedia(incidentPhotosAndVideos);

        incident.setTitle(incidentRequestDto.getTitle());
        incident.setDescription(incidentRequestDto.getDescription());
        incident.setUser(user);
        incident.setLocation(location);
        incident.setIncidentType(incidentType);
        incident.setCreatedAt(LocalDateTime.now());

        incidentRepository.save(incident);

        List<Photo> incidentPhotos = mediaServiceResult.getPhotos();
        List<Video> incidentVideos = mediaServiceResult.getVideos();

        for (Photo photo : incidentPhotos) {

            mediaService.createIncidentPhoto(incident, photo);
        }

        for (Video video : incidentVideos) {
            mediaService.createIncidentVideo(incident, video);
        }

        return incident;
    }

    @Override
    public void deleteById(Long id) {
        incidentRepository.deleteById(id);
    }
}
