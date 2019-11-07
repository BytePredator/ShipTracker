package it.univaq.byte_predator.shiptracker.Models;

import com.google.android.gms.maps.model.LatLng;

public class LatLon {
    private static final int EARTH_RADIUS = 6371; // Approx Earth radius in KM

    protected double latitude;
    protected double longitude;

    public LatLon(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public LatLng getLatLng(){ return new LatLng(latitude, longitude); }

    public double distance(LatLon b){
        double dLat  = Math.toRadians((b.latitude - this.latitude));
        double dLong = Math.toRadians((b.longitude - this.longitude));

        double startLat = Math.toRadians(this.latitude);
        double endLat   = Math.toRadians(b.latitude);

        double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    private double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }
}
