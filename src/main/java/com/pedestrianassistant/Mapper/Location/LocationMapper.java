package com.pedestrianassistant.Mapper.Location;

import com.pedestrianassistant.Dto.Response.Location.LocationResponseDto;
import com.pedestrianassistant.Model.Location.Location;

public class LocationMapper {

    public static LocationResponseDto toDto(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }

        LocationResponseDto locationResponseDto = new LocationResponseDto();

        locationResponseDto.setId(location.getId());
        locationResponseDto.setAddress(location.getAddress());
        locationResponseDto.setLatitude(location.getLatitude());
        locationResponseDto.setLongitude(location.getLongitude());

        return locationResponseDto;
    }
}
