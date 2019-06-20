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

    static public void CREATE(SQLiteDatabase db){
        String sql = "CREATE TABLE "+TABLE+" ( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NAME + " varchar(20) NOT NULL, " +
                SYNC + " BOOLEAN NOT NULL DEFAULT 1, " +
                DELETE+" BOOLEAN NOT NULL DEFAULT 0" +
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
        Cursor cursor = db.query(TABLE, new String[]{ID,NAME}, DELETE+"=0", null, null, null, null, null);
        while (cursor.moveToNext())
            r.add(genTrack(cursor.getLong(0), cursor.getString(1)));
        return r;
    }

    static private Track genTrack(long Id, String Name){
        return new Track(Id, Name, waypointsTable.getBoasByTrack(Id));
    }

    static public ArrayList<Track> getSync(){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        ArrayList<Track> r = new ArrayList<>();
        Cursor cursor = db.query(TABLE, new String[]{ID,NAME},
                SYNC+"='?'",new String[]{"false"},null,null, ID);
        while(cursor.moveToNext())
            r.add(genTrack(cursor.getLong(0), cursor.getString(1)));
        return r;
    }

    static public long UpdateTrack(Track data){
        return UpdateTrack(data, false);
    }

    static public long InsertTrack(Track data){
        return InsertTrack(data, false);
    }

    static private long UpdateTrack(Track data, boolean sync){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NAME, data.getName());
        values.put(SYNC, sync);
        long id = data.getId();
        for (Boa boa: data.getBoas()) {
            waypointsTable.SaveWaypoints(id, boa.getId());
        }
        return db.update(TABLE, values, ID+" = ?", new String[]{String.valueOf(id)});
    }

    static private long InsertTrack(Track data, boolean sync){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NAME, data.getName());
        values.put(SYNC, sync);
        long Id = db.insert(TABLE, null, values);
        ArrayList<Boa> boas = data.getBoas();
        for(Boa boa: boas){
            waypointsTable.InsertWaypoint(Id, boa.getId());
        }
        return Id;
    }

    static public int DeleteTrack(long Id){
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
        HelperHTTP.getInstance(context).RequestJSONObject("http://10.10.0.49/get.php?table=tracks", "GET",
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ArrayList<Track> r = new ArrayList<Track>();
                        try {
                            SQLiteDatabase db;
                            if(response.getInt("error")==0){
                                db = HelperDatabase.getInstance().getWritableDatabase();
                                JSONArray array = response.getJSONArray("data");
                                for (int i=0; i<array.length(); i++) {
                                    JSONArray item = array.getJSONArray(i);
                                    Track t;
                                    if((t = getTrack(item.getLong(0)))!=null){
                                        t.setName(item.getString(1));
                                        JSONArray jboas = item.getJSONArray(3);
                                        for (int j=0; j < jboas.length(); j++){
                                            JSONArray boa = jboas.getJSONArray(j);
                                            Boa b = new Boa(boa.getLong(0),boa.getDouble(1), boa.getDouble(2));
                                            //TODO: salvare tutte le boe prima
                                            //boasTable.saveBoa(b);
                                            t.addBoa(b);
                                        }
                                        UpdateTrack(t);
                                        JSONArray jraces = item.getJSONArray(4);
                                        for (int j=0; j < jraces.length(); j++)
                                            racesTable.SaveRace(new Race(jraces.getLong(j), new ArrayList<Point>()), t.getId(), true);
                                        Log.w("trackModel", "update: "+String.valueOf(t.getId()));
                                    }else{
                                        ArrayList<Boa> boas = new ArrayList<>();
                                        JSONArray jboas = item.getJSONArray(3);
                                        for (int j=0; j < jboas.length(); j++){
                                            JSONArray boa = jboas.getJSONArray(j);
                                            Boa b = new Boa(boa.getLong(0),boa.getDouble(1), boa.getDouble(2));
                                            //TODO: salvare tutte le boe prima
                                            //boasTable.saveBoa(b);
                                            boas.add(b);
                                        }
                                        t = new Track(item.getLong(0),
                                                item.getString(1),
                                                boas);
                                        InsertTrack(t, true);
                                        JSONArray jraces = item.getJSONArray(4);
                                        for (int j=0; j < jraces.length(); j++)
                                            racesTable.SaveRace(new Race(jraces.getLong(j), new ArrayList<Point>()), t.getId(), true);
                                        Log.w("trackModel", "insert: "+String.valueOf(t.getId()));
                                    }
                                    r.add(t);
                                }
                            }else
                                r = getTracks();
                        } catch (JSONException e) {
                            r = getTracks();
                        }
                        if(callback != null)
                            callback.callback(r);
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
}
