package it.univaq.byte_predator.shiptracker.Tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import it.univaq.byte_predator.shiptracker.Helper.HelperDatabase;
import it.univaq.byte_predator.shiptracker.Models.Boa;
import it.univaq.byte_predator.shiptracker.Models.Point;
import it.univaq.byte_predator.shiptracker.Models.Waypoint;

public class waypointsTable {
    static public String TABLE = "waypoints";
    static public String BOA = "IdBoa";
    static public String TRACK = "IdTrack";


    static public void CREATE(SQLiteDatabase db){
        String sql = "CREATE TABLE "+TABLE+" (" +
                "  "+BOA+" INTEGER NOT NULL," +
                "  "+TRACK+" INTEGER NOT NULL" +
                ")";
        db.execSQL(sql);
    }

    static public void UPDATE(SQLiteDatabase db){
        db.execSQL("DROP TABLE "+TABLE+";");
        CREATE(db);
    }

    static public boolean findWaypoint(long idTrack, long idBoa){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        Cursor cursor = db.query(TABLE, new String[]{BOA,TRACK}, BOA+"=? AND "+TRACK+"=?", new String[]{String.valueOf(idBoa), String.valueOf(idTrack)}, null, null, null, null);
        return cursor.getCount() == 1;
    }

    static private Waypoint genWaypoint(long Id, long idTrack, long idBoa){
        return new Waypoint(Id, idBoa, idTrack);
    }

    static public void SaveWaypoints(long idTrack, long idBoa){
        //Log.w("waypoint get id", String.valueOf(w.getId()));
        if(!findWaypoint(idTrack, idBoa)) {
            //Log.w("debug", "insert");
            InsertWaypoint(idTrack, idBoa);
        }
    }

    static public long InsertWaypoint(long idTrack, long idBoa){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BOA, idBoa);
        values.put(TRACK, idTrack);
        return db.insert(TABLE, null, values);
    }

    static public long RemoveWaypoint(long idTrack, long idBoa){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BOA, idBoa);
        values.put(TRACK, idTrack);
        return db.delete(TABLE, TRACK+"=? AND "+BOA+"=?", new String[]{String.valueOf(idTrack), String.valueOf(idBoa)});
    }

    static public ArrayList<Boa> getBoasByTrack(long idTrack){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        ArrayList<Boa> r = new ArrayList<>();
        Cursor cursor = db.query(TABLE, new String[]{BOA}, TRACK+"=?", new String[]{String.valueOf(idTrack)}, null, null, null);
        while (cursor.moveToNext()){
            r.add(boasTable.getBoa(cursor.getInt(0)));
        }
        return r;
    }

}
