package com.pedestrianassistant.Dto.Response.Core;

import com.pedestrianassistant.Dto.Response.Location.LocationResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentResponseDto {
    private String title;
    private String description;
    private String username;
    private LocationResponseDto locationResponseDto;
    private IncidentTypeResponseDto incidentTypeResponseDto;
    private LocalDateTime createdAt;
}
