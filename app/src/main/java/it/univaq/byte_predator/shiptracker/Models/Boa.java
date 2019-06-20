package it.univaq.byte_predator.shiptracker.Models;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by byte-predator on 22/02/18.
 */

public class Boa extends LatLon{

    private long Id;
    private Marker marker;

    public Boa(long Id, double latitude, double longitude) {
        this(Id, latitude, longitude, null);
    }

    public Boa(long Id, double latitude, double longitude, Marker marker){
        this.Id = Id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.marker = marker;
    }

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public Marker getMarker() {
        return this.marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public Boa clone(){
        return new Boa(this.Id, this.latitude, this.longitude, this.marker);
    }

    public String toString(){
        return "[Id: "+this.Id+" Lat: "+this.latitude+" Lon: "+this.longitude+"]";
    }
}
