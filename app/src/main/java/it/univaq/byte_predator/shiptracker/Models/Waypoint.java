package it.univaq.byte_predator.shiptracker.Models;

/**
 * Created by byte-predator on 23/02/18.
 */

public class Waypoint {
    private long Id;
    private long IdBoa;
    private long IdTrack;

    public  Waypoint(long IdBoa, long IdTrack){
        this(0, IdBoa, IdTrack);
    }

    public Waypoint(long Id, long IdBoa, long IdTrack){
        this.Id = Id;
        this.IdBoa = IdBoa;
        this.IdTrack = IdTrack;
    }

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public long getIdBoa() {
        return IdBoa;
    }

    public void setIdBoa(long idBoa) {
        IdBoa = idBoa;
    }

    public long getIdTrack() {
        return IdTrack;
    }

    public void setIdTrack(long idTrack) {
        IdTrack = idTrack;
    }
}
