package com.pedestrianassistant.Dto.Request.Location;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationRequestDto {
    private Long id;
    private String address;
    private Double latitude;
    private Double longitude;
}
