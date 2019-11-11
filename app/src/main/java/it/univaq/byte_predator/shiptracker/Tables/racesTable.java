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

import it.univaq.byte_predator.shiptracker.Helper.CONF;
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
    static public String ID = "Id";
    static public String TRACK = "Track";
    static private String TIME = "Time";
    static private String SYNC = "Sync";
    static private String NEW = "New";
    static private String DELETE = "Del";
    static private String SERVER = CONF.SERVER;

    static public void CREATE(SQLiteDatabase db){
        String sql = "CREATE TABLE "+TABLE+" ( " +
                ID+" INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TRACK+" INTEGER NOT NULL, " +
                TIME+" INTEGER NOT NULL, " +
                SYNC+" BOOLEAN NOT NULL DEFAULT 1, " +
                DELETE+" BOOLEAN NOT NULL DEFAULT 0," +
                NEW+" BOOLEAN NOT NULL DEFAULT 0" +
                ")";
        db.execSQL(sql);
    }

    static public void UPDATE(SQLiteDatabase db){
        db.execSQL("DROP TABLE "+TABLE+";");
        CREATE(db);
    }

    static public Race getRace(long Id){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        Cursor cursor = db.query(TABLE, new String[]{ID, TIME}, ID+"=? AND "+DELETE+"=0", new String[]{String.valueOf(Id)}, null, null, null, null);
        if(cursor.moveToNext())
            return genRace(cursor.getLong(0), cursor.getInt(1));
        return null;
    }

    static public ArrayList<Race> getRacesByTrack(long trackId){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        ArrayList<Race> r = new ArrayList<>();
        Cursor cursor = db.query(TABLE, new String[]{ID, TIME}, TRACK+"=? AND "+DELETE+"=0", new String[]{String.valueOf(trackId)}, null, null, null, null);
        while (cursor.moveToNext())
            r.add(genRace(cursor.getLong(0), cursor.getInt(1)));

        return r;
    }

    static public ArrayList<Race> getRaces(){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        ArrayList<Race> r = new ArrayList<>();
        Cursor cursor = db.query(TABLE, new String[]{ID, TIME},  DELETE+"=0", null, null, null, null, null);
        while (cursor.moveToNext())
            r.add(genRace(cursor.getLong(0), cursor.getInt(1)));
        return r;
    }

    static public Long getTrackIdByRaceId(long RaceId){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        Cursor cursor = db.query(TABLE, new String[]{TRACK}, ID+"=?", new String[]{String.valueOf(RaceId)}, null, null, null, null);
        if(cursor.moveToNext())
            return cursor.getLong(0);
        return null;
    }

    static public Track getTrackByRaceId(long RaceId){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        Cursor cursor = db.query(TABLE, new String[]{TRACK}, ID+"=?", new String[]{String.valueOf(RaceId)}, null, null, null, null);
        if(cursor.moveToNext())
            return tracksTable.getTrack(cursor.getLong(0));
        return null;
    }

    static private Race genRace(long Id, int time){
        Log.w("race table", "id:"+Id+" n: "+positionsTable.getPositionsByRace(Id));
        return new Race(Id, time, positionsTable.getPositionsByRace(Id));
    }

    static public ArrayList<Race> getSync(){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        ArrayList<Race> r = new ArrayList<>();
        Cursor cursor = db.query(TABLE, new String[]{ID, TIME},
                SYNC+"=?",new String[]{"true"},null,null, ID);
        while(cursor.moveToNext())
            r.add(genRace(cursor.getLong(0), cursor.getInt(1)));
        return r;
    }

    static public ArrayList<Race> getSyncDel(){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        ArrayList<Race> r = new ArrayList<>();
        Cursor cursor = db.query(TABLE, new String[]{ID,TIME},
                SYNC+"=1 AND "+DELETE+"=1",null,null,null, ID);
        while(cursor.moveToNext())
            r.add(genRace(cursor.getLong(0), cursor.getInt(1)));
        return r;
    }

    static public ArrayList<Race> getSyncNew(){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        ArrayList<Race> r = new ArrayList<>();
        Cursor cursor = db.query(TABLE, new String[]{ID,TIME},
                SYNC+"=1 AND "+NEW+"=1",null,null,null, ID);
        while(cursor.moveToNext())
            r.add(genRace(cursor.getLong(0), cursor.getInt(1)));
        return r;
    }

    static public ArrayList<Race> getSyncEdit(){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        ArrayList<Race> r = new ArrayList<>();
        Cursor cursor = db.query(TABLE, new String[]{ID,TIME},
                SYNC+"=1 AND "+DELETE+"=0 AND "+NEW+"=0",null,null,null, ID);
        while(cursor.moveToNext())
            r.add(genRace(cursor.getLong(0), cursor.getInt(1)));
        return r;
    }

    static public long Save(Race data, long TrackId){
        return Save(data, TrackId, false);
    }

    static public long Save(Race data, long TrackId, boolean sync){
        if(getRace(data.getId()) != null)
            return Update(data, TrackId, sync);
        else
            return Insert(data, TrackId, sync);
    }

    static public long Update(Race data, long TrackId){
        return Update(data, TrackId, false);
    }

    static public long Insert(Race data, long TrackId){
        return Insert(data, TrackId, true);
    }

    static public long Update(Race data, long TrackId, boolean sync){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ID, data.getId());
        values.put(TRACK, TrackId);
        values.put(TIME, data.getTime());
        values.put(SYNC, sync);
        Log.w("raceTable", "update: "+data.getId()+" "+data.getTime());
        long r = db.update(TABLE, values, ID+" = ?", new String[]{String.valueOf(data.getId())});
        for(Point p: data.getPoints()){
            positionsTable.Save(p,1,data.getId(), sync);
        }
        return r;
    }

    static public void updateId(long old, long id){
        /*SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ID, id);
        db.update(TABLE, values, ID+" = ?", new String[]{String.valueOf(old)});
        //positionsTable.updateRaceId(old, id);*/
    }

    static public void synced(long id){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SYNC, false);
        values.put(NEW, false);
        db.update(TABLE, values, ID+" = ?", new String[]{String.valueOf(id)});
    }

    static public long Insert(Race data, long TrackId, boolean sync){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        if(data.getId() != 0)
            values.put(ID, data.getId());
        values.put(TRACK, TrackId);
        values.put(TIME, data.getTime());
        values.put(SYNC, sync);
        values.put(NEW, sync?true:false);
        Log.w("raceTable", "insert: "+data.getId()+" "+data.getTime());
        long Id = db.insert(TABLE, null, values);
        ArrayList<Point> points = data.getPoints();
        for(Point point: points){
            positionsTable.Insert(point, 1,data.getId(), sync);
        }
        return Id;
    }

    static public int Delete(long Id){
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

    static private void realDelete(long Id){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        db.delete(TABLE, ID+"=?", new String[]{String.valueOf(Id)});
    }

    static public void getFromServer(final Context context){
        getFromServer(context, null);
    }

    static public void getFromServer(final Context context, final DataCallback callback){
        HashMap<String, String> params = new HashMap<>();
        params.put("table", "races");

        HelperHTTP.getInstance(context).RequestJSONObject("http://"+SERVER+"/get.php", params, "POST",
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ArrayList<Race> r = new ArrayList<Race>();
                        boolean changed = false;
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
                                        Update(ra, item.getLong(1));
                                    }else{
                                        ArrayList<Point> points = new ArrayList<>();
                                        JSONArray jpoints = item.getJSONArray(3);
                                        for (int j=0; j < jpoints.length(); j++){
                                            JSONArray point = jpoints.getJSONArray(j);
                                            points.add(new Point(point.getLong(0),point.getDouble(1), point.getDouble(2), point.getInt(3)));
                                        }
                                        ra = new Race(item.getLong(0),
                                                0,
                                                points);
                                        Insert(ra, item.getLong(1), true);
                                    }
                                    r.add(ra);
                                }
                            }else
                                r = getRaces();
                        } catch (JSONException e) {
                            r = getRaces();
                        }
                        if(callback != null)
                            callback.callback(r, changed);
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
