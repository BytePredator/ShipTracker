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
import java.util.HashMap;
import java.util.Iterator;

import it.univaq.byte_predator.shiptracker.Helper.CONF;
import it.univaq.byte_predator.shiptracker.Helper.DataCallback;
import it.univaq.byte_predator.shiptracker.Helper.HelperDatabase;
import it.univaq.byte_predator.shiptracker.Helper.HelperHTTP;
import it.univaq.byte_predator.shiptracker.Models.Boa;
import it.univaq.byte_predator.shiptracker.Models.Point;
import it.univaq.byte_predator.shiptracker.Models.Race;
import it.univaq.byte_predator.shiptracker.Models.Track;
import it.univaq.byte_predator.shiptracker.Models.Waypoint;

/**
 * Created by byte-predator on 21/02/18.
 */

public class tracksTable {
    static private String TABLE = "tracks";
    static private String ID = "Id";
    static private String NAME = "Name";
    static private String SYNC = "Sync";
    static private String DELETE = "Del";
    static private String NEW = "new";
    static private String SERVER = CONF.SERVER;

    static public void CREATE(SQLiteDatabase db){
        String sql = "CREATE TABLE "+TABLE+" ( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NAME + " varchar(20) NOT NULL, " +
                SYNC + " BOOLEAN NOT NULL DEFAULT 1, " +
                DELETE+" BOOLEAN NOT NULL DEFAULT 0," +
                NEW+" BOOLEAN NOT NULL DEFAULT 0" +
                ")";
        db.execSQL(sql);
    }

    static public void UPDATE(SQLiteDatabase db){
        db.execSQL("DROP TABLE "+TABLE+";");
        CREATE(db);
    }

    static public Track getTrackByRace(long Id){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT t."+ID+", t."+NAME+" FROM "+TABLE+" AS t LEFT JOIN races ON races.Id = t."+ID+" WHERE races.Id=?", new String[]{String.valueOf(Id)});
        if(cursor.moveToNext())
            return genTrack(cursor.getLong(0), cursor.getString(1));
        return null;
    }

    static public Track getTrack(long Id){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        Cursor cursor = db.query(TABLE, new String[]{ID,NAME}, ID+"=? AND "+DELETE+"=0", new String[]{String.valueOf(Id)}, null, null, null, null);
        if(cursor.moveToNext())
            return genTrack(cursor.getLong(0), cursor.getString(1));
        return null;
    }

    static public ArrayList<Track> getTracks(){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        ArrayList<Track> r = new ArrayList<>();
        Cursor cursor = db.query(TABLE, new String[]{ID,NAME}, DELETE+"=0", null, null, null, NAME, null);
        while (cursor.moveToNext())
            r.add(genTrack(cursor.getLong(0), cursor.getString(1)));
        return r;
    }

    static private Track genTrack(long Id, String Name){
        return new Track(Id, Name, waypointsTable.getWaypointsByTrack(Id), racesTable.getRacesByTrack(Id));
    }

    static public ArrayList<Track> getSync(){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        ArrayList<Track> r = new ArrayList<>();
        Cursor cursor = db.query(TABLE, new String[]{ID,NAME},
                SYNC+"=1",null,null,null, ID);
        while(cursor.moveToNext())
            r.add(genTrack(cursor.getLong(0), cursor.getString(1)));
        return r;
    }

    static public ArrayList<Track> getSyncDel(){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        ArrayList<Track> r = new ArrayList<>();
        Cursor cursor = db.query(TABLE, new String[]{ID,NAME},
                SYNC+"=1 AND "+DELETE+"=1",null,null,null, ID);
        while(cursor.moveToNext())
            r.add(genTrack(cursor.getLong(0), cursor.getString(1)));
        return r;
    }

    static public ArrayList<Track> getSyncNew(){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        ArrayList<Track> r = new ArrayList<>();
        Cursor cursor = db.query(TABLE, new String[]{ID,NAME},
                SYNC+"=1 AND "+NEW+"=1",null,null,null, ID);
        while(cursor.moveToNext())
            r.add(genTrack(cursor.getLong(0), cursor.getString(1)));
        return r;
    }

    static public ArrayList<Track> getSyncEdit(){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        ArrayList<Track> r = new ArrayList<>();
        Cursor cursor = db.query(TABLE, new String[]{ID,NAME},
                SYNC+"=1 AND "+DELETE+"=0 AND "+NEW+"=0",null,null,null, ID);
        while(cursor.moveToNext())
            r.add(genTrack(cursor.getLong(0), cursor.getString(1)));
        return r;
    }

    static public long Save(Track data){
        return Save(data, false);
    }

    static public long Save(Track data, boolean sync){
        if(getTrack(data.getId()) != null) {
            return Update(data, sync);
        }else{
            return Insert(data, sync);
        }
    }

    static public long Update(Track data){
        return Update(data, false);
    }

    static public long Insert(Track data){
        return Insert(data, true);
    }

    static private long Update(Track data, boolean sync){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NAME, data.getName());
        values.put(SYNC, sync);
        long id = data.getId();
        Log.w("trackTable", "update: "+String.valueOf(data.getId()));
        db.update(TABLE, values, ID+" = ?", new String[]{String.valueOf(id)});
        for (Waypoint waypoint: data.getWaypoints()) {
            waypointsTable.Save(waypoint, id, sync);
        }
        for (Race race: data.getRaces()) {
            racesTable.Save(race, id, sync);
        }
        return id;
    }

    static private void updateId(long old, long id){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ID, id);
        db.update(TABLE, values, ID+" = ?", new String[]{String.valueOf(old)});
        waypointsTable.updateTrackId(old, id);
    }

    static private void synced(long id){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SYNC, false);
        values.put(NEW, false);
        db.update(TABLE, values, ID+" = ?", new String[]{String.valueOf(id)});
    }

    static private long Insert(Track data, boolean sync){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        if(data.getId() != 0) {
            values.put(ID, data.getId());
        }
        values.put(NAME, data.getName());
        values.put(SYNC, sync);
        values.put(NEW, sync?true:false);
        Log.w("trackTable", "insert: "+String.valueOf(data.getId()));
        long Id = db.insert(TABLE, null, values);
        for(Waypoint waypoint: data.getWaypoints()){
            waypointsTable.Insert(waypoint, Id, sync);
        }
        for(Race race: data.getRaces()){
            racesTable.Insert(race, Id, sync);
        }
        return Id;
    }

    static public int Delete(long Id){
        Log.w("tracks table","delete: "+Id);
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DELETE, true);
        values.put(SYNC, true);
        waypointsTable.DeleteByTrack(Id);
        racesTable.DeleteByTrack(Id);
        return db.update(TABLE, values, ID+" = ?", new String[]{String.valueOf(Id)});
    }

    static private void realDelete(long Id){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        db.delete(TABLE, ID+"=?", new String[]{String.valueOf(Id)});
    }

    static public void getFromServer(final Context context){
        getFromServer(context, null);
    }

    static public void getFromServer(final Context context, final DataCallback callback){
        HashMap<String, String> params = new HashMap<>();
        params.put("table", "tracks");

        HelperHTTP.getInstance(context).RequestJSONObject("http://"+SERVER+"/get.php", params, "POST",
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ArrayList<Track> r = new ArrayList<Track>();
                        boolean changed = false;
                        try {
                            SQLiteDatabase db;
                            if(response.getInt("error")==0){
                                db = HelperDatabase.getInstance().getWritableDatabase();
                                JSONArray array = response.getJSONArray("data");
                                for (int i=0; i<array.length(); i++) {
                                    JSONArray item = array.getJSONArray(i);
                                    Track t;
                                    if((t = getTrack(item.getLong(0)))!=null){    //Update track
                                        Track old_t = t.clone();
                                        t.setName(item.getString(1));
                                        t.clearWaypoints();
                                        t.clearRaces();
                                        JSONArray jwaypoints = item.getJSONArray(3);
                                        for (int j=0; j < jwaypoints.length(); j++){
                                            JSONArray waypoint = jwaypoints.getJSONArray(j);
                                            JSONArray jboas = waypoint.getJSONArray(1);
                                            Boa b = new Boa(
                                                    jboas.getLong(0),
                                                    jboas.getDouble(1),
                                                    jboas.getDouble(2)
                                            );
                                            Waypoint w = new Waypoint(waypoint.getLong(0), b, waypoint.getLong(2));
                                            t.addWaypoint(w);
                                        }


                                        JSONArray jraces = item.getJSONArray(4);
                                        for (int j=0; j < jraces.length(); j++) {
                                            JSONArray jrace = jraces.getJSONArray(j);
                                            Race race = new Race(
                                                    jrace.getLong(0),
                                                    jrace.getInt(1),
                                                    new ArrayList<Point>()
                                            );
                                            t.addRace(race);
                                        }
                                        if(! t.isEqual(old_t)) {
                                            tracksTable.Update(t);
                                            changed = true;
                                        }
                                    }else{                                             //Insert Track
                                        ArrayList<Waypoint> waypoints = new ArrayList<>();

                                        JSONArray jwaypoints = item.getJSONArray(3);
                                        for (int j=0; j < jwaypoints.length(); j++){
                                            JSONArray waypoint = jwaypoints.getJSONArray(j);
                                            JSONArray jboas = waypoint.getJSONArray(1);
                                            Boa b = new Boa(
                                                    jboas.getLong(0),
                                                    jboas.getDouble(1),
                                                    jboas.getDouble(2)
                                            );
                                            Waypoint w = new Waypoint(waypoint.getLong(0), b, waypoint.getLong(2));
                                            waypoints.add(w);
                                        }

                                        ArrayList<Race> races = new ArrayList<>();

                                        JSONArray jraces = item.getJSONArray(4);
                                        for (int j=0; j < jraces.length(); j++) {
                                            JSONArray race = jraces.getJSONArray(j);
                                            Race tmp = new Race(
                                                    race.getLong(0),
                                                    race.getInt(1),
                                                    new ArrayList<Point>()
                                            );
                                            races.add(tmp);
                                        }

                                        t = new Track(item.getLong(0),
                                                item.getString(1),
                                                waypoints,
                                                races);

                                        tracksTable.Insert(t, false);
                                        changed = true;
                                    }
                                    r.add(t);
                                }
                            }else
                                r = getTracks();
                        } catch (JSONException e) {
                            r = getTracks();
                        }
                        if(callback != null)
                            callback.callback(r, changed);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("tracksTable","get from server");
                    }
                }
        );
    }

    static public void sendToServer(final Context context){
        sendToServer(context, null);
    }

    static public void sendToServer(final Context context, final DataCallback callback){
        JSONObject params = new JSONObject();
        try {
            params.accumulate("table", "tracks");

            JSONArray arr = new JSONArray();
            for(Track track: tracksTable.getSyncNew()){
                arr.put(track.toJSON());
            }
            params.accumulate("new",arr);

            arr = new JSONArray();
            for(Track track: tracksTable.getSyncEdit()){
                arr.put(track.toJSON());
            }
            params.accumulate("edit",arr);

            arr = new JSONArray();
            for(Track track: tracksTable.getSyncDel()){
                arr.put(track.toJSON());
            }
            params.accumulate("delete",arr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HelperHTTP.getInstance(context).RequestJSONObject("http://"+SERVER+"/set.php", params, "POST",new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject data = response.getJSONObject("data");
                    JSONArray added = data.getJSONArray("new");
                    for(int i=0; i < added.length(); i++){
                        JSONArray row = added.getJSONArray(i);
                        long old = row.getLong(0);
                        long id = row.getLong(1);
                        if(old != id){
                            updateId(old, id);
                        }
                        synced(id);
                    }
                    JSONArray waypoints = data.getJSONArray("waypoints");
                    for(int i=0; i< waypoints.length(); i++){
                        JSONArray row = waypoints.getJSONArray(i);
                        long old = row.getLong(0);
                        long id = row.getLong(1);
                        if(old != id){
                            Log.w("tracks table", "update waypoint id "+old+" to "+id);
                            waypointsTable.updateId(old, id);
                        }
                        waypointsTable.synced(id);
                    }
                    JSONArray del   = data.getJSONArray("del");
                    for(int i=0; i < del.length(); i++){
                        realDelete(del.getLong(i));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                ArrayList<JSONObject> arr = new ArrayList<>();
                arr.add(response);
                if(callback != null)
                    callback.callback(arr, false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("tracksTable", "send to server");
            }
        });
    }
}
