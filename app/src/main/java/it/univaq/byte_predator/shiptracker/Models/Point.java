package it.univaq.byte_predator.shiptracker.Models;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by byte-predator on 22/02/18.
 */

public class Point extends LatLon{
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
}
