package it.univaq.byte_predator.shiptracker.Tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import it.univaq.byte_predator.shiptracker.Helper.CONF;
import it.univaq.byte_predator.shiptracker.Helper.HelperDatabase;
import it.univaq.byte_predator.shiptracker.Models.Boa;
import it.univaq.byte_predator.shiptracker.Models.Point;
import it.univaq.byte_predator.shiptracker.Models.Waypoint;

public class waypointsTable {
    static public String TABLE = "waypoints";
    static public String ID = "Id";
    static public String BOA = "IdBoa";
    static public String TRACK = "IdTrack";
    static public String NUMBER = "number";
    static public String SYNC = "Sync";
    static public String DELETE = "Del";
    static private String SERVER = CONF.SERVER;


    static public void CREATE(SQLiteDatabase db){
        String sql = "CREATE TABLE "+TABLE+" (" +
                ID+" INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BOA+" INTEGER NOT NULL, " +
                TRACK+" INTEGER NOT NULL, " +
                NUMBER+" INTEGER NOT NULL, " +
                SYNC+" BOOLEAN NOT NULL DEFAULT 1, " +
                DELETE+" BOOLEAN NOT NULL DEFAULT 0" +
                ")";
        db.execSQL(sql);
    }

    static public void UPDATE(SQLiteDatabase db){
        db.execSQL("DROP TABLE "+TABLE+";");
        CREATE(db);
    }

    static public boolean findWaypoint(long idTrack, long idBoa, long number){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        Cursor cursor = db.query(TABLE, new String[]{BOA,TRACK}, BOA+"=? AND "+TRACK+"=? AND "+NUMBER+"=?", new String[]{String.valueOf(idBoa), String.valueOf(idTrack), String.valueOf(number)}, null, null, null, null);
        return cursor.getCount() == 1;
    }

    static public Waypoint getWaypoint(long Id){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        Cursor cursor = db.query(TABLE, null, ID+"=?", new String[]{String.valueOf(Id)}, null, null, null, null);
        if(cursor.moveToNext())
            return genWaypoint(cursor.getLong(0), cursor.getLong(1), cursor.getLong(3));
        return null;
    }

    static public ArrayList<Waypoint> getWaypointsByTrack(long idTrack){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        ArrayList<Waypoint> r = new ArrayList<>();
        Cursor cursor = db.query(TABLE, null, TRACK+"=?", new String[]{String.valueOf(idTrack)}, null, null, null);
        while (cursor.moveToNext()){
            r.add(genWaypoint(cursor.getLong(0), cursor.getLong(1), cursor.getLong(3)));
        }
        return r;
    }

    static private Waypoint genWaypoint(long Id, long idBoa, long number){
        return new Waypoint(Id, boasTable.getBoa(idBoa), number);
    }

    static public ArrayList<Waypoint> getSync(){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        ArrayList<Waypoint> w = new ArrayList<>();
        Cursor cursor = db.query(TABLE, new String[]{ID},
                SYNC+"='?'",new String[]{"false"},null,null, ID);
        while(cursor.moveToNext())
            w.add(genWaypoint(cursor.getLong(0), cursor.getLong(1), cursor.getLong(3)));
        return w;
    }

    static public long Save(Waypoint data, long TrackId){
        return Save(data, TrackId, false);
    }

    static public long Save(Waypoint data, long TrackId, boolean sync){
        if(getWaypoint(data.getId()) != null) {
            return Update(data, TrackId, sync);
        }else{
            return Insert(data, TrackId, sync);
        }
    }

    static public long Update(Waypoint data, long TrackId){
        return Update(data, TrackId, false);
    }

    static public long Insert(Waypoint data, long TrackId){
        return Insert(data, TrackId, true);
    }

    static public long Update(Waypoint data, long TrackId, boolean sync){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ID, data.getId());
        values.put(BOA, data.getBoa().getId());
        values.put(TRACK, TrackId);
        values.put(NUMBER, data.getNumber());
        values.put(SYNC, sync);
        Log.w("waypointTable", "update: "+String.valueOf(data.getId()));
        return db.update(TABLE, values, ID+" = ?", new String[]{String.valueOf(data.getId())});
    }

    static public void updateId(long old, long id){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ID, id);
        db.update(TABLE, values, ID+" = ?", new String[]{String.valueOf(old)});
    }

    static public void updateTrackId(long old, long id){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TRACK, id);
        db.update(TABLE, values, TRACK+" = ?", new String[]{String.valueOf(old)});
    }

    static public void synced(long id){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SYNC, false);
        //values.put(NEW, false);
        db.update(TABLE, values, ID+" = ?", new String[]{String.valueOf(id)});
    }

    static public long Insert(Waypoint data, long TrackId, boolean sync){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        if(data.getId() != 0)
            values.put(ID, data.getId());
        values.put(BOA, data.getBoa().getId());
        values.put(TRACK, TrackId);
        values.put(NUMBER, data.getNumber());
        values.put(SYNC, sync);
        Log.w("waypointTable", "insert: "+String.valueOf(data.getId()));
        return db.insert(TABLE, null, values);
    }

    static public long Delete(long Id){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DELETE, true);
        values.put(SYNC, true);
        return db.update(TABLE, values, ID+" = ?", new String[]{String.valueOf(Id)});
    }

    static public long DeleteByTrack(long Id){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DELETE, true);
        values.put(SYNC, true);
        return db.update(TABLE, values, TRACK+" = ?", new String[]{String.valueOf(Id)});
    }

}
