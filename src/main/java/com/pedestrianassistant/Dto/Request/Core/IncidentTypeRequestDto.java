package com.pedestrianassistant.Dto.Request.Core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentTypeRequestDto {
    private String name;
    private String description;
}
