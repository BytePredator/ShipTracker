package it.univaq.byte_predator.shiptracker.Models;

/**
 * Created by byte-predator on 21/02/18.
 */

public class Trace {
    private long Id;
    private Race race;

    public Trace(long Id, Race race) {
        this.Id = Id;
        this.race = race;
    }

    public long getId() { return Id; }

    public void setId(long id) { Id = id; }

    public Race getRace() {
        return race.clone();
    }

    public void setRace(Race race) {
        this.race = race.clone();
    }
}
