package it.univaq.byte_predator.shiptracker.Models;

import android.location.Location;

/**
 * Created by byte-predator on 22/02/18.
 */

public class Point extends LatLon{
    public static final int THRESHOLD_DIST = 10;

    private long Id;
    private int time;

    public Point(long Id, double latitude, double longitude) {
        this(Id, latitude, longitude, 0);
    }

    public Point(long Id, double latitude, double longitude, int time){
        this.Id = Id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
    }

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public Point clone(){
        return new Point(this.Id, this.latitude, this.longitude, this.time);
    }

    public String toString(){
        return "[Id: "+this.Id+" Lat: "+this.latitude+" Lon: "+this.longitude+" time: "+this.time+"]";
    }

    public boolean inRange(Waypoint w){
        Location l = new Location("");
        Location l2 = new Location("");
        l.setLatitude(this.latitude);
        l.setLongitude(this.longitude);
        l2.setLatitude(w.getBoa().getLatitude());
        l2.setLongitude(w.getBoa().getLongitude());
        return l.distanceTo(l2) < THRESHOLD_DIST;
    }
}
