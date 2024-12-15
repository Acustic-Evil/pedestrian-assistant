package com.pedestrianassistant.Mapper.Core;

import com.pedestrianassistant.Model.Core.IncidentType;
import com.pedestrianassistant.Dto.Response.Core.IncidentTypeResponseDto;

public class IncidentTypeMapper {

    public static IncidentTypeResponseDto toDto(IncidentType incidentType) {
        if (incidentType == null) {
            throw new IllegalArgumentException("IncidentType cannot be null");
        }

        return new IncidentTypeResponseDto(
                incidentType.getId(),
                incidentType.getName(),
                incidentType.getDescription()
        );
    }
}

