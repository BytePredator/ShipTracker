package it.univaq.byte_predator.shiptracker.Models;

import java.util.ArrayList;

/**
 * Created by byte-predator on 21/02/18.
 */

public class Track {

    private long Id;
    private String name;
    private Double distance;
    private ArrayList<Boa> boas = new ArrayList<>();
    private ArrayList<Race> races = new ArrayList<>();

    public Track(long Id, String name){
        this(Id, name, new ArrayList<Boa>());
    }

    public Track(long Id, String name, ArrayList<Boa> boas){
        this.Id = Id;
        this.name = name;
        this.distance = 0d;
        Boa old = null;
        for(Boa boa: boas){
            Boa tmp = boa.clone();
            this.boas.add(tmp);
            if(old != null)
                this.distance += tmp.distance(old);
            old = tmp;
        }
    }

    public int getBestTime(){
        int time = -1;
        for (Race race: this.races )
            if(time == -1 || race.getTime() < time)
                time = race.getTime();
        return time<0?0:time;
    }

    public long getId() { return Id; }

    public void setId(long Id) { this.Id = Id; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer boasNumber() {  return this.boas.size();  }

    public Double getDistance() {  return this.distance;  }

    public void addBoa(Boa boa){
        this.boas.add(boa.clone());
    }

    public void removeBoa(int n){
        if(n >= 0 && n < this.boas.size())
            this.boas.remove(n);
    }

    public Boa getBoa(int n){
        if(n >= 0 && n < this.boas.size())
            return this.boas.get(n);
        throw new IndexOutOfBoundsException();
    }

    public void addRace(Race race){
        this.races.add(race);
    }

    public ArrayList<Boa> getBoas(){
        ArrayList<Boa> r = new ArrayList<>();
        for(Boa boa:this.boas){
            r.add(boa.clone());
        }
        return r;
    }

    public ArrayList<Boa> getUniqueBoas(){
        ArrayList<Boa> r = new ArrayList<>();
        for(Boa boa:this.boas){
            boolean f = false;
            for (Boa b: r)
                if(b.getId() == boa.getId()){
                    f = true;
                    break;
                }
            if(!f)
                r.add(boa.clone());
        }
        return r;
    }

    public void clearBoas(){
        this.boas.clear();
    }
}
