package it.univaq.byte_predator.shiptracker.Helper;

import com.google.android.gms.maps.model.LatLng;

import it.univaq.byte_predator.shiptracker.Models.Point;

public class HelperLatLng {

    public static double bearingFrom2Points(Point current, Point next){
        double dLng = next.getLongitude()-current.getLongitude();

        double y = Math.sin(dLng) * Math.cos(next.getLatitude());
        double x = Math.cos(current.getLatitude()) * Math.sin(next.getLatitude()) - Math.sin(current.getLatitude())
                * Math.cos(next.getLatitude()) * Math.cos(dLng);

        double brng = Math.atan2(y, x);

        return Math.toDegrees(-brng);
    }

    public static double bearingFrom2LatLng(LatLng current, LatLng next){
        double dLng = next.longitude-current.longitude;

        double y = Math.sin(dLng) * Math.cos(next.latitude);
        double x = Math.cos(current.latitude) * Math.sin(next.latitude) - Math.sin(current.latitude)
                * Math.cos(next.latitude) * Math.cos(dLng);

        double brng = Math.atan2(y, x);

        return Math.toDegrees(-brng);
    }
}
