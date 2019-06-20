package it.univaq.byte_predator.shiptracker.Tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import it.univaq.byte_predator.shiptracker.Helper.DataCallback;
import it.univaq.byte_predator.shiptracker.Helper.HelperDatabase;
import it.univaq.byte_predator.shiptracker.Helper.HelperHTTP;
import it.univaq.byte_predator.shiptracker.Models.Point;
import it.univaq.byte_predator.shiptracker.Models.Race;
import it.univaq.byte_predator.shiptracker.Models.Track;
import it.univaq.byte_predator.shiptracker.Models.Waypoint;

/**
 * Created by byte-predator on 21/02/18.
 */

public class racesTable {
    static private String TABLE = "races";
    static private String ID = "Id";
    static private String TRACK = "Track";
    static private String SYNC = "Sync";
    static private String DELETE = "Del";

    static public void CREATE(SQLiteDatabase db){
        String sql = "CREATE TABLE "+TABLE+" ( " +
                ID+" INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TRACK+" INTEGER NOT NULL, " +
                SYNC+" BOOLEAN NOT NULL DEFAULT 1, " +
                DELETE+" BOOLEAN NOT NULL DEFAULT 0" +
                ")";
        db.execSQL(sql);
    }

    static public void UPDATE(SQLiteDatabase db){
        db.execSQL("DROP TABLE "+TABLE+";");
        CREATE(db);
    }

    static public Race getRace(long Id){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        Cursor cursor = db.query(TABLE, new String[]{ID}, ID+"=?", new String[]{String.valueOf(Id)}, null, null, null, null);
        if(cursor.moveToNext())
            return genRace(cursor.getLong(0));
        return null;
    }

    static public ArrayList<Race> getRaces(long trackId){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        ArrayList<Race> r = new ArrayList<>();
        Cursor cursor = db.query(TABLE, new String[]{ID}, TRACK+"=?", new String[]{String.valueOf(trackId)}, null, null, null, null);
        while (cursor.moveToNext())
            r.add(genRace(cursor.getLong(0)));
        return r;
    }

    static public ArrayList<Race> getRaces(){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        ArrayList<Race> r = new ArrayList<>();
        Cursor cursor = db.query(TABLE, new String[]{ID}, null, null, null, null, null, null);
        while (cursor.moveToNext())
            r.add(genRace(cursor.getLong(0)));
        return r;
    }

    static public Track getTrackByRaceId(long RaceId){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        Cursor cursor = db.query(TABLE, new String[]{TRACK}, ID+"=?", new String[]{String.valueOf(RaceId)}, null, null, null, null);
        if(cursor.moveToNext())
            return tracksTable.getTrack(cursor.getLong(0));
        return null;
    }

    static private Race genRace(long Id){
        return new Race(Id,  positionsTable.getPositionsByRace(Id));
    }

    static public ArrayList<Race> getSync(){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        ArrayList<Race> r = new ArrayList<>();
        Cursor cursor = db.query(TABLE, new String[]{ID},
                SYNC+"='?'",new String[]{"false"},null,null, ID);
        while(cursor.moveToNext())
            r.add(genRace(cursor.getLong(0)));
        return r;
    }

    static public long SaveRace(Race data, long TrackId){
        return SaveRace(data, TrackId, false);
    }

    static public long SaveRace(Race data, long TrackId, boolean sync){
        if(getRace(data.getId()) != null)
            return UpdateRace(data, TrackId, sync);
        else
            return InsertRace(data, TrackId, sync);
    }

    static public long UpdateRace(Race data, long TrackId){
        return UpdateRace(data, TrackId, false);
    }

    static public long InsertRace(Race data, long TrackId){
        return InsertRace(data, TrackId, false);
    }

    static private long UpdateRace(Race data, long TrackId, boolean sync){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TRACK, TrackId);
        values.put(SYNC, sync);
        Log.w("debug", "update race");
        return db.update(TABLE, values, ID+" = ?", new String[]{String.valueOf(data.getId())});
    }

    static private long InsertRace(Race data, long TrackId, boolean sync){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TRACK, TrackId);
        values.put(SYNC, sync);
        Log.w("debug", "insert race");
        long Id = db.insert(TABLE, null, values);
        ArrayList<Point> points = data.getPoints();
        for(Point point: points){
            //TODO: pointsTable.insertPoint
            //waypointsTable.InsertWaypoint(db, new Waypoint(point.getId(),Id));
        }
        return Id;
    }

    static public int DeleteRace(long Id){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DELETE, true);
        values.put(SYNC, true);
        return db.update(TABLE, values, ID+" = ?", new String[]{String.valueOf(Id)});
    }

    static public void getFromServer(final Context context){
        getFromServer(context, null);
    }

    static public void getFromServer(final Context context, final DataCallback callback){
        HelperHTTP.getInstance(context).RequestJSONObject("http://10.10.0.49/get.php?table=races", "GET",
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ArrayList<Race> r = new ArrayList<Race>();
                        try {
                            if(response.getInt("error")==0){
                                JSONArray array = response.getJSONArray("data");
                                for (int i=0; i<array.length(); i++) {
                                    JSONArray item = array.getJSONArray(i);
                                    Race ra;
                                    if((ra = getRace(item.getLong(0)))!=null){
                                        JSONArray jpoints = item.getJSONArray(2);
                                        for (int j=0; j < jpoints.length(); j++){
                                            JSONArray point = jpoints.getJSONArray(j);
                                            ra.addPoint(new Point(point.getLong(0),point.getDouble(1), point.getDouble(2), point.getInt(3)));
                                        }
                                        UpdateRace(ra, item.getLong(1));
                                        Log.w("raceModel", "update: "+String.valueOf(ra.getId()));
                                    }else{
                                        ArrayList<Point> points = new ArrayList<>();
                                        JSONArray jpoints = item.getJSONArray(3);
                                        for (int j=0; j < jpoints.length(); j++){
                                            JSONArray point = jpoints.getJSONArray(j);
                                            points.add(new Point(point.getLong(0),point.getDouble(1), point.getDouble(2), point.getInt(3)));
                                        }
                                        ra = new Race(item.getLong(0),
                                                points);
                                        InsertRace(ra, item.getLong(1), true);
                                        Log.w("raceModel", "insert: "+String.valueOf(ra.getId()));
                                    }
                                    r.add(ra);
                                }
                            }else
                                r = getRaces();
                        } catch (JSONException e) {
                            r = getRaces();
                        }
                        if(callback != null)
                            callback.callback(r);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("racesTable","get from server");
                    }
                }
        );
    }
}
