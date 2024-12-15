package com.pedestrianassistant.Dto.Response.Location;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponseDto {
    private Long id;
    private String address;
    private Double latitude;
    private Double longitude;
}
