package it.univaq.byte_predator.shiptracker.Models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by byte-predator on 21/02/18.
 */

public class Track {

    private long Id;
    private String name;
    private Double distance;
    private ArrayList<Waypoint> waypoints = new ArrayList<>();
    private ArrayList<Race> races = new ArrayList<>();

    public Track(long Id, String name){
        this(Id, name, new ArrayList<Waypoint>());
    }


    public Track(long Id, String name, ArrayList<Waypoint> waypoints){ this(Id, name, waypoints, new ArrayList<Race>());}

    public Track(long Id, String name, ArrayList<Waypoint> waypoints, ArrayList<Race> races){
        this.Id = Id;
        this.name = name;
        this.distance = 0d;
        Boa old = null;
        for(Waypoint waypoint: waypoints){
            Waypoint tmp = waypoint.clone();
            this.waypoints.add(tmp);
            if(old != null) {
                this.distance += tmp.getBoa().distance(old);
                Log.w("track", "d:"+tmp.getBoa().distance(old));
            }
            old = tmp.getBoa();
        }
        for(Race race: races)
            this.races.add(race.clone());
    }

    public int getBestTime(){
        int time = 0;
        for (Race race: this.races )
            if (race.getTime() > 0 && (time == 0 || race.getTime() < time))
                time = race.getTime();
        return time;
    }

    public long getId() { return Id; }

    public void setId(long Id) { this.Id = Id;}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer waypointsNumber() {  return this.waypoints.size();  }

    public Double getDistance() {  return this.distance;  }

    public void addWaypoint(Waypoint waypoint){
        this.waypoints.add(waypoint.clone());
    }

    public void addRace(Race race){
        this.races.add(race.clone());
    }

    public void removeWaypoint(int n){
        if(n >= 0 && n < this.waypoints.size())
            this.waypoints.remove(n);
    }

    public void removeRace(int n){
        if(n >= 0 && n < this.races.size())
            this.races.remove(n);
    }

    public Waypoint getWaypoint(int n){
        if(n >= 0 && n < this.waypoints.size())
            return this.waypoints.get(n);
        throw new IndexOutOfBoundsException();
    }

    public Race getRace(int n){
        if(n >= 0 && n < this.races.size())
            return this.races.get(n);
        throw new IndexOutOfBoundsException();
    }

    public ArrayList<Waypoint> getWaypoints(){
        ArrayList<Waypoint> r = new ArrayList<>();
        for(Waypoint waypoint:this.waypoints){
            r.add(waypoint.clone());
        }
        return r;
    }

    public ArrayList<Race> getRaces(){
        ArrayList<Race> r = new ArrayList<>();
        for(Race race:this.races){
            r.add(race.clone());
        }
        return r;
    }

    public ArrayList<Boa> getUniqueBoas(){
        ArrayList<Boa> r = new ArrayList<>();
        for(Waypoint waypoint:this.waypoints){
            boolean f = false;
            for (Boa b: r)
                if(b.getId() == waypoint.getBoa().getId()){
                    f = true;
                    break;
                }
            if(!f)
                r.add(waypoint.getBoa().clone());
        }
        return r;
    }

    public void clearWaypoints(){
        this.waypoints.clear();
    }

    public void clearRaces(){
        this.races.clear();
    }

    public Track clone(){
        return new Track(this.Id, this.name, this.waypoints, this.races);
    }

    public boolean isEqual(Track t){
        if(this.getId() != t.getId())
            return false;
        if(!this.getName().equals(t.getName()))
            return false;
        if(!this.getDistance().equals(t.getDistance()))
            return false;
        for(Waypoint w1 : this.waypoints){
            boolean f = false;
            for(Waypoint w2 : t.waypoints)
                if(w1.isEqual(w2)){
                    f = true;
                    break;
                }
            if(!f)
                return false;
        }

        for(Race r1 : this.races){
            boolean f = false;
            for(Race r2 : t.races)
                if(r1.isEqual(r2)){
                    f = true;
                    break;
                }
            if(!f)
                return false;
        }

        return true;
    }

    public JSONObject toJSON(){
        JSONObject obj = new JSONObject();

        try {
            obj.accumulate("id", this.getId());
            obj.accumulate("name", this.getName());
            JSONArray arr = new JSONArray();
            for (Waypoint waypoint: this.waypoints) {
                arr.put(waypoint.toJSON());
            }
            obj.accumulate("waypoints", arr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }
}
