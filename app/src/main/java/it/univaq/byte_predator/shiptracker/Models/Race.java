package it.univaq.byte_predator.shiptracker.Models;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import it.univaq.byte_predator.shiptracker.Tables.racesTable;
import it.univaq.byte_predator.shiptracker.Tables.tracksTable;
import it.univaq.byte_predator.shiptracker.Tables.waypointsTable;

public class Race {

    /*private class Stat {
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
    }*/

    private long Id;
    private ArrayList<Point> points = new ArrayList<>();
    private ArrayList<Waypoint> waypoints = new ArrayList<>();
    //private ArrayList<Stat> stats = new ArrayList<>();
    private double distance;
    private int time, currentWaypoint, currentPoint, currentTime;
    private ArrayList<Integer> waypoints_times = new ArrayList<>();

    public Race(long Id, int time, ArrayList<Point> points){
        this.Id = Id;
        this.currentWaypoint = 0;
        this.currentPoint = 0;
        for (Point p: points) {
            this.points.add(p.clone());
        }
        if(this.points.size()>0)
            this.currentTime = this.points.get(0).getTime();
        else
            this.currentTime = 0;
        this.distance = 0;
        this.time = time;
        if(this.trackId() != null)
            this.waypoints = waypointsTable.getWaypointsByTrack(this.trackId());
        this.calcWaypointsTimes();
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

    public Point getPoint(int index) {
        return this.points.get(index).clone();
    }

    public void setPoints(ArrayList<Point> points) {
        this.points.clear();
        for (Point p: points) {
            this.points.add(p.clone());
        }
        if(this.points.size()>0)
            this.currentTime = this.points.get(0).getTime();
        else
            this.currentTime = 0;
        this.stats();
        this.calcWaypointsTimes();
    }

    public int getCurrentWaypoint() {
        return this.currentWaypoint;
    }
    public int getCurrentPoint() {
        return this.currentPoint;
    }
    public int getCurrentTime() {
        return this.currentTime;
    }

    public void nextPoint(){
        if(this.currentPoint+1 > this.points.size())
            return;

        this.currentPoint++;
        this.currentTime = this.points.get(this.currentPoint).getTime();

        if(this.currentPointInRange())
            this.currentWaypoint++;
    }

    public void setWaypoints(ArrayList<Waypoint> waypoints) {
        this.waypoints.clear();
        for (Waypoint p: waypoints) {
            this.waypoints.add(p.clone());
        }
        this.calcWaypointsTimes();
    }

    public void addPoint(Point point){
        Point last = this.points.get(this.points.size()-1);
        this.points.add(point.clone());
        //this.time = point.getTime()-this.points.get(0).getTime();
        if(last != null)
            this.distance += last.distance(point);
        if(this.inNewWaypoint(point)){
            this.currentWaypoint++;
            //this.stats.add(new Stat(this.time, this.distance));
        }
    }

    public Long trackId(){
        return racesTable.getTrackIdByRaceId(this.Id);
    }

    public Track track(){
        return tracksTable.getTrackByRace(this.Id);
    }

    private boolean inNewWaypoint(Point p){
        Waypoint waypoint = this.waypoints.get(this.currentWaypoint);
        return waypoint != null && p.inRange(waypoint);
    }

    private void stats(){
        this.distance = 0;
        Point last = null;
        for (Point p: this.points) {
            if(last != null)
                this.distance += last.distance(p);
            last = p;
        }
        if(this.points.size()>0)
            this.time = this.points.get(this.points.size()-1).getTime() - this.points.get(0).getTime();
    }

    private void calcWaypointsTimes(){
        int curr = 0;
        for (Point p: this.points) {
            if(curr < this.waypoints.size() && p.inRange(this.waypoints.get(curr))){
                this.waypoints_times.add(new Integer(p.getTime()));
                curr++;
            }

        }
    }

    public ArrayList<Boa> completedBoas(){
        ArrayList<Boa> boas = new ArrayList<>();
        for (int i = 0; i <= this.currentWaypoint && i< this.waypoints.size(); i++){
            Boa boa = this.waypoints.get(i).getBoa();
            if(!isInList(boas, boa))
                boas.add(boa);
        }
        return boas;
    }

    public Boa nextBoa(){
        if(this.currentWaypoint+1 < this.waypoints.size())
            return this.waypoints.get(this.currentWaypoint+1).getBoa();
        return null;
    }

    public ArrayList<Boa> remaningBoas(){
        ArrayList<Boa> boas = new ArrayList<>();
        ArrayList<Boa> completed = this.completedBoas();
        Boa next = this.nextBoa();
        for (int i = this.currentWaypoint; i< this.waypoints.size(); i++){
            Boa boa = this.waypoints.get(i).getBoa();
            if(!isInList(completed, boa) && !isInList(boas, boa) && next.getId() != boa.getId())
                boas.add(boa);
        }
        return boas;
    }

    public void setCurrentTime(int time){
        if(time < 0)
            time = 0;
        if(time > this.time)
            time = this.time;
        int max = 0;
        for(int i = 0; i < this.waypoints_times.size(); i++){
            int tmp = this.waypoints_times.get(i)-this.points.get(0).getTime();
            if(tmp <= time && tmp >= max ){
                max = tmp;
                this.currentWaypoint = i;
            }
        }


        max = 0;
        for(int i = 0; i < this.points.size(); i++){
            int tmp = this.points.get(i).getTime()-this.points.get(0).getTime();
            if(tmp <= time && tmp >= max){
                max = tmp;
                this.currentPoint = i;
            }
        }

        this.currentTime = time + this.points.get(0).getTime();
    }

    private boolean isInList(ArrayList<Boa> list, Boa boa){
        for (Boa b : list){
            if(boa.getId() == b.getId())
                return true;
        }
        return  false;
    }

    public boolean currentPointInRange(){
        if(this.waypoints.size() <= this.currentWaypoint+1)
            return false;
        return this.points.get(this.currentPoint).inRange(this.waypoints.get(this.currentWaypoint+1));
    }

    public Race clone(){
        Race r = new Race(this.Id, this.time, (ArrayList<Point>) this.points.clone());
        r.waypoints = ((ArrayList<Waypoint>) this.waypoints.clone());
        r.distance = this.distance;
        r.time = this.time;
        r.currentWaypoint = this.currentWaypoint;
        r.currentPoint = this.currentPoint;
        r.currentTime = this.currentTime;
        r.waypoints_times = this.waypoints_times;
        //r.stats = (ArrayList<Stat>) this.stats.clone();
        return r;
    }

    public boolean isEqual(Race r){
        if(this.getId() != r.getId())
            return false;
        if(this.getTime() != r.getTime())
            return false;

        for(Point p1 : this.points){
            boolean f = false;
            for(Point p2 : r.points)
                if(p1.isEqual(p2)){
                    f = true;
                    break;
                }
            if(!f)
                return false;
        }
        return true;
    }

}
