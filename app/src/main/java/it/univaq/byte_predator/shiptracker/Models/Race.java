package it.univaq.byte_predator.shiptracker.Models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import it.univaq.byte_predator.shiptracker.Tables.tracksTable;

public class Race {

    private static double DELTA = 10;

    private class Stat {
        private int time;
        private double distance;

        public Stat(int time, double distance){
            this.time = time;
            this.distance = distance;
        }

        public double getDistance() {
            return distance;
        }

        public void setDistance(double distance) {
            this.distance = distance;
        }

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }

        public Stat clone(){
            return new Stat(this.time, this.distance);
        }
    }

    private long Id;
    private ArrayList<Point> points = new ArrayList<>();
    private ArrayList<Boa> waypoints = new ArrayList<>();
    private ArrayList<Stat> stats = new ArrayList<>();
    private double distance;
    private int time;
    private int currentWaypoint;

    public Race(long Id, ArrayList<Point> points){
        this.Id = Id;
        this.currentWaypoint = 0;
        for (Point p: points) {
            this.points.add(p.clone());
        }
        this.distance = 0;
        this.time = 0;
    }

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getTime() {
        return time;
    }

    public String getDate(){
        return new SimpleDateFormat("yyyy/MM/dd").format(new java.util.Date(this.time*1000));
    }

    public void setTime(int time) {
        this.time = time;
    }

    public ArrayList<Point> getPoints() {
        ArrayList<Point> r = new ArrayList<>();
        for (Point p: this.points) {
            r.add(p.clone());
        }
        return r;
    }

    public void setPoints(ArrayList<Point> points) {
        this.points.clear();
        for (Point p: points) {
            this.points.add(p.clone());
        }
    }

    public int getCurrentWaypoint() {
        return this.currentWaypoint;
    }

    public void setWaypoints(ArrayList<Boa> waypoints) {
        this.waypoints.clear();
        for (Boa p: waypoints) {
            this.waypoints.add(p.clone());
        }
        this.stats();
    }

    public void addPoint(Point point){
        Point last = this.points.get(this.points.size()-1);
        this.points.add(point.clone());
        this.time = point.getTime()-this.points.get(0).getTime();
        if(last != null)
            this.distance += last.distance(point);
        if(this.inNewWaypoint(point)){
            this.currentWaypoint++;
            this.stats.add(new Stat(this.time, this.distance));
        }
    }

    public Track track(){
        return tracksTable.getTrackByRace(this.Id);
    }

    private boolean inNewWaypoint(Point p){
        Boa waypoint = this.waypoints.get(this.currentWaypoint);
        return waypoint != null && waypoint.distance(p) <= DELTA;
    }

    private void stats(){
        this.time = 0;
        this.stats.clear();
        this.distance = 0;
        this.currentWaypoint = 0;
        Point last = null;
        for (Point p: this.points) {
            if(last != null)
                this.distance += last.distance(p);
            if(this.inNewWaypoint(p)){
                this.currentWaypoint++;
                this.stats.add(new Stat(p.getTime() - this.points.get(0).getTime(), this.distance));
            }
            last = p;
        }
        if(this.points.size()>0)
            this.time = this.points.get(this.points.size()-1).getTime() - this.points.get(0).getTime();
    }

    public Race clone(){
        Race r = new Race(this.Id, (ArrayList<Point>) this.points.clone());
        r.waypoints = ((ArrayList<Boa>) this.waypoints.clone());
        r.distance = this.distance;
        r.time = this.time;
        r.currentWaypoint = this.currentWaypoint;
        r.stats = (ArrayList<Stat>) this.stats.clone();
        return r;
    }

}
