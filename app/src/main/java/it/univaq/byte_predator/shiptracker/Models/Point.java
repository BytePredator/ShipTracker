package it.univaq.byte_predator.shiptracker.Models;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
        super(latitude, longitude);
        this.Id = Id;
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

    public boolean isEqual(Point p){
        if(this.getId() != p.getId())
            return false;
        if(this.getLatitude() != p.getLatitude())
            return false;
        if(this.getLongitude() != p.getLongitude())
            return false;
        if(this.getTime() != p.getTime())
            return false;
        return true;
    }

    public JSONObject toJSON(){
        JSONObject obj = new JSONObject();

        try {
            obj.accumulate("id", this.getId());
            obj.accumulate("latitude", this.getLatitude());
            obj.accumulate("longitude", this.getLongitude());
            obj.accumulate("time", this.getTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }
}
