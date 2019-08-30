package it.univaq.byte_predator.shiptracker.Models;

/**
 * Created by byte-predator on 23/02/18.
 */

public class Waypoint {
    private long Id;
    private Boa boa;
    private long number;

    public  Waypoint(Boa boa, long number){
        this(0, boa, number);
    }

    public Waypoint(long Id, Boa boa, long number){
        this.Id = Id;
        this.boa = boa;
        this.number = number;
    }

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public Boa getBoa() {
        return boa;
    }

    public void setBoa(Boa boa) {
        this.boa = boa;
    }

    public long getNumber() { return number; }

    public void setNumber(long number) { this.number = number; }

    public Waypoint clone(){
        return new Waypoint(this.Id, this.boa, this.number);
    }
}
