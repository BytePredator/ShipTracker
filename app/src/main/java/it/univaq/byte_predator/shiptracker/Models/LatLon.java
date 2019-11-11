package it.univaq.byte_predator.shiptracker.Models;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class LatLon {
    private static final int EARTH_RADIUS = 6371; // Approx Earth radius in KM
    public static final int THRESHOLD_DIST = 10;

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

    public boolean inRange(Waypoint w){
        return this.inRange(w.getBoa());
    }

    public boolean inRange(LatLon lalo){
        Location l2 = new Location("");
        l2.setLatitude(lalo.latitude);
        l2.setLongitude(lalo.longitude);
        return this.inRange(l2);
    }

    public boolean inRange(Location l2){
        Location l = new Location("");
        l.setLatitude(this.latitude);
        l.setLongitude(this.longitude);
        return l.distanceTo(l2) < THRESHOLD_DIST;
    }
}
