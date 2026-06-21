package com.nongxinle.route.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class GeoPoint {
    private String lat;
    private String lng;

    public GeoPoint() {
    }

    public GeoPoint(String lat, String lng) {
        this.lat = lat;
        this.lng = lng;
    }
}
