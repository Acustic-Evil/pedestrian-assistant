package com.pedestrianassistant.Service.Location;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NominatimResponse {

    @JsonProperty("display_name")
    private String displayName;

    private Address address;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {
        @JsonProperty("city")
        private String city;

        @JsonProperty("county")
        private String county;

        @JsonProperty("state")
        private String state;

        @JsonProperty("region")
        private String region;

        @JsonProperty("country")
        private String country;

        public String getEffectiveLocation() {
            if (city != null) return city;
            if (county != null) return county;
            if (state != null) return state;
            if (region != null) return region;
            return "Unknown location";
        }
    }
}
