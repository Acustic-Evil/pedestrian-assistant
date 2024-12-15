package com.pedestrianassistant.Mapper.Core;

import com.pedestrianassistant.Dto.Response.Core.IncidentResponseDto;
import com.pedestrianassistant.Dto.Response.Core.IncidentTypeResponseDto;
import com.pedestrianassistant.Dto.Response.Location.LocationResponseDto;
import com.pedestrianassistant.Mapper.Location.LocationMapper;
import com.pedestrianassistant.Model.Core.Incident;

public class IncidentMapper {

    public static IncidentResponseDto toDto(Incident incident) {
        if (incident == null) {
            throw new IllegalArgumentException("Incident cannot be null");
        }

        IncidentResponseDto dto = new IncidentResponseDto();

        dto.setTitle(incident.getTitle());
        dto.setDescription(incident.getDescription());
        dto.setUsername(incident.getUser() != null ? incident.getUser().getUsername() : null);

        LocationResponseDto locationResponseDto =
                incident.getLocation() != null ? LocationMapper.toDto(incident.getLocation()) : null;

        IncidentTypeResponseDto incidentTypeResponseDto =
                incident.getIncidentType() != null ? IncidentTypeMapper.toDto(incident.getIncidentType()) : null;

        dto.setLocationResponseDto(locationResponseDto);
        dto.setIncidentTypeResponseDto(incidentTypeResponseDto);

        dto.setCreatedAt(incident.getCreatedAt());

        return dto;
    }
}
