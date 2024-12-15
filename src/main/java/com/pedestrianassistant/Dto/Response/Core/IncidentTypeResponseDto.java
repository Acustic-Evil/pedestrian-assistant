package com.pedestrianassistant.Dto.Response.Core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentTypeResponseDto {
    private Long id;
    private String name;
    private String description;
}
