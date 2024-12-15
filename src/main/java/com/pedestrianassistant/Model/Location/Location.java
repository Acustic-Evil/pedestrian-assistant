package com.pedestrianassistant.Model.Location;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "address")
    private String address;

    @Column(name = "latitude", nullable = false, unique = true)
    private Double latitude;

    @Column(name = "longitude", nullable = false, unique = true)
    private Double longitude;

}
