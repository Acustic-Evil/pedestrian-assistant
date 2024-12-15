package com.pedestrianassistant.Dto.Request.Core;

import com.pedestrianassistant.Dto.Request.Location.LocationRequestDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentRequestDto {
    private String title;
    private String description;
    private String username;
    private LocationRequestDto locationRequestDto;
    private Long incidentTypeId;
}
