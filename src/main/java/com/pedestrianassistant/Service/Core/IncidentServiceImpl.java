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
import com.pedestrianassistant.Util.Specification.IncidentSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    public List<Incident> findByTitle(String title) {
        return incidentRepository.findByTitleContainingIgnoreCase(title);
    }

    @Override
    public List<Incident> findByDescription(String description) {
        return incidentRepository.findByDescriptionContainingIgnoreCase(description);
    }

    @Override
    public List<Incident> findByUsername(String username) {
        return incidentRepository.findByUser_Username(username);
    }

    @Override
    public List<Incident> findByCreatedAt(String date) {
        return incidentRepository.findByCreatedAt(date);
    }

    @Override
    public List<Incident> findByCreatedAtBetween(String startDate, String endDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

            LocalDate parsedStartDate = LocalDate.parse(startDate, formatter);
            LocalDate parsedEndDate = LocalDate.parse(endDate, formatter);

            return incidentRepository.findByCreatedAtBetween(parsedStartDate, parsedEndDate);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format, expected DD-MM-YYYY");
        }
    }

    @Override
    public List<Incident> findByLocationAddressContaining(String addressPart) {
        return incidentRepository.findByLocationAddressContaining(addressPart);
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

        Location location = locationService.findById(locationRequestDto.getId())
                .orElseGet(() -> {
                    return locationService.save(locationRequestDto);
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

    public List<Incident> findByFilters(String title, String description, Long userId,
                                        Long locationId, String address, Long incidentTypeId,
                                        LocalDateTime startDate, LocalDateTime endDate) {

        Specification<Incident> spec = Specification.where(IncidentSpecifications.hasTitleLike(title))
                .and(IncidentSpecifications.hasDescriptionLike(description))
                .and(IncidentSpecifications.hasUserId(userId))
                .and(IncidentSpecifications.hasLocation(locationId, address))
                .and(IncidentSpecifications.hasIncidentTypeId(incidentTypeId))
                .and(IncidentSpecifications.createdBetween(startDate, endDate));

        return incidentRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
