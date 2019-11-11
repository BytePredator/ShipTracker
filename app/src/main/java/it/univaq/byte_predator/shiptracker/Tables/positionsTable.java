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

/**
 * Created by byte-predator on 21/02/18.
 */

public class positionsTable {
    static private String TABLE = "positions";
    static private String ID = "Id";
    static private String TYPE = "Type";
    static private String REF = "IdRef";
    static private String LAT = "Latitude";
    static private String LNG = "Longitude";
    static private String TIME = "Time";
    static private String SYNC = "Sync";
    static private String NEW = "new";
    static private String DELETE = "Del";
    static private String SERVER = CONF.SERVER;

    static public void CREATE(SQLiteDatabase db){
        String sql = "CREATE TABLE "+TABLE+" (" +
                ID+" INTEGER PRIMARY KEY AUTOINCREMENT," +
                TYPE+" INTEGER NOT NULL, " +
                REF+" INTEGER NOT NULL, " +
                LAT+" DOUBLE NOT NULL, " +
                LNG+" DOUBLE NOT NULL, " +
                TIME+" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                SYNC+" BOOLEAN NOT NULL DEFAULT 1, " +
                NEW+" BOOLEAN NOT NULL DEFAULT 0," +
                DELETE+" BOOLEAN NOT NULL DEFAULT 0" +
                ")";
        db.execSQL(sql);
    }

    static public void UPDATE(SQLiteDatabase db){
        db.execSQL("DROP TABLE "+TABLE+";");
        CREATE(db);
    }

    static public Point getPosition(long Id){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        Cursor cursor = db.query(TABLE, new String[]{ID, LAT, LNG, TIME}, ID+"=? AND "+DELETE+"=0", new String[]{String.valueOf(Id)}, null, null, null, null);
        if(cursor.moveToNext())
            return genPosition(cursor.getLong(0), cursor.getDouble(1), cursor.getDouble(2), cursor.getInt(3));
        return null;
    }

    static public ArrayList<Point> getPositionsByRace(long Id){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        ArrayList<Point> points = new ArrayList<Point>();
        Cursor cursor = db.query(TABLE, new String[]{ID, LAT, LNG, TIME}, DELETE+"=0 AND "+TYPE+"=1 AND "+REF+"=?", new String[]{String.valueOf(Id)}, null, null, null, null);
        while(cursor.moveToNext()) {
            //Log.w("positions table", "race: "+Id+" id:"+cursor.getLong(0));
            points.add(genPosition(cursor.getLong(0), cursor.getDouble(1), cursor.getDouble(2), cursor.getInt(3)));
        }
        return points;
    }

    static private Point genPosition(long id, double lat, double lng, int time){
        return new Point(id, lat, lng, time);
    }

    static public ArrayList<Point> getSync(){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        ArrayList<Point> p = new ArrayList<>();
        Cursor cursor = db.query(TABLE, new String[]{ID, LAT, LNG, TIME},
                SYNC+"=?",new String[]{"true"},null,null, ID);
        while(cursor.moveToNext())
            p.add(genPosition(cursor.getLong(0), cursor.getDouble(1), cursor.getDouble(2), cursor.getInt(3)));
        return p;
    }

    static public ArrayList<Point> getSyncDel(){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        ArrayList<Point> r = new ArrayList<>();
        Cursor cursor = db.query(TABLE, new String[]{ID, LAT, LNG, TIME},
                SYNC+"=1 AND "+DELETE+"=1",null,null,null, ID);
        while(cursor.moveToNext())
            r.add(genPosition(cursor.getLong(0), cursor.getDouble(1), cursor.getDouble(2), cursor.getInt(3)));
        return r;
    }

    static public ArrayList<Point> getSyncNew(){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        ArrayList<Point> r = new ArrayList<>();
        Cursor cursor = db.query(TABLE, new String[]{ID, LAT, LNG, TIME},
                SYNC+"=1 AND "+NEW+"=1",null,null,null, ID);
        while(cursor.moveToNext())
            r.add(genPosition(cursor.getLong(0), cursor.getDouble(1), cursor.getDouble(2), cursor.getInt(3)));
        return r;
    }

    static public ArrayList<Point> getSyncEdit(){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        ArrayList<Point> r = new ArrayList<>();
        Cursor cursor = db.query(TABLE, new String[]{ID, LAT, LNG, TIME},
                SYNC+"=1 AND "+DELETE+"=0 AND "+NEW+"=0",null,null,null, ID);
        while(cursor.moveToNext())
            r.add(genPosition(cursor.getLong(0), cursor.getDouble(1), cursor.getDouble(2), cursor.getInt(3)));
        return r;
    }

    static public long Save(Point data, int type, long refId){
        return Save(data, type, refId, false);
    }

    static public long Save(Point data, int type, long refId, boolean sync){
        if(getPosition(data.getId()) != null)
            return Update(data, type, refId, sync);
        else
            return Insert(data, type, refId, sync);
    }

    static public long Update(Point data, int type, long refId){
        return Update(data, type, refId, false);
    }

    static public long Insert(Point data, int type, long refId){
        return Insert(data, type, refId, true);
    }

    static public long Update(Point data, int type, long refId, boolean sync){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ID, data.getId());
        values.put(LAT, data.getLatitude());
        values.put(LNG, data.getLongitude());
        values.put(TIME, data.getTime());
        values.put(TYPE, type);
        values.put(REF, refId);
        values.put(SYNC, sync);
        Log.w("positionsTable", "update: "+data.getId()+" "+data.getTime());
        return db.update(TABLE, values, ID+" = ?", new String[]{String.valueOf(data.getId())});
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

    static public long Insert(Point data, int type, long refId, boolean sync){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        if(data.getId() != 0)
            values.put(ID, data.getId());
        values.put(LAT, data.getLatitude());
        values.put(LNG, data.getLongitude());
        values.put(TIME, data.getTime());
        values.put(TYPE, type);
        values.put(REF, refId);
        values.put(SYNC, sync);
        values.put(NEW, sync?true:false);
        Log.w("positionsTable", "insert: "+data.getId()+" "+data.getTime());
        return db.insert(TABLE, null, values);
    }

    static public int Delete(long Id){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DELETE, true);
        values.put(SYNC, true);
        return db.update(TABLE, values, ID+" = ?", new String[]{String.valueOf(Id)});
    }

    static public long DeleteByRace(long Id){
        return DeleteByRef(1, Id);
    }

    static public long DeleteByTrace(long Id){
        return DeleteByRef(2, Id);
    }

    static public long DeleteByRef(int type, long refId){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DELETE, true);
        values.put(SYNC, true);
        return db.update(TABLE, values, REF+" = ? AND "+TYPE+" = ?", new String[]{String.valueOf(refId), String.valueOf(type)});
    }

    static private void realDelete(long Id){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        db.delete(TABLE, ID+"=?", new String[]{String.valueOf(Id)});
    }

    static public void getTraceFromServer(final Context context, long traceId){
        getTraceFromServer(context, traceId, null);
    }

    static public void getTraceFromServer(final Context context, long traceId, final DataCallback callback){
        getFromServer(context, 2, traceId, callback);
    }

    static public void getRaceFromServer(final Context context, long raceId){
        getRaceFromServer(context, raceId, null);
    }

    static public void getRaceFromServer(final Context context, long raceId, final DataCallback callback){
        getFromServer(context, 1, raceId, callback);
    }

    static public void getFromServer(final Context context, final int type, final long raceId, final DataCallback callback){
        HashMap<String, String> params = new HashMap<>();
        params.put("table", "race");
        params.put("raceId", String.valueOf(raceId));

        HelperHTTP.getInstance(context).RequestJSONObject("http://"+SERVER+"/get.php", params, "POST",
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ArrayList<Point> p = new ArrayList<>();
                        boolean changed = false;
                        try {
                            if(response.getInt("error")==0){
                                JSONArray array = response.getJSONArray("data");
                                for (int i=0; i<array.length(); i++) {
                                    JSONArray item = array.getJSONArray(i);
                                    Point point;
                                    if((point = getPosition(item.getLong(0)))!=null){
                                        point.setLatitude(item.getDouble(1));
                                        point.setLongitude(item.getDouble(2));
                                        point.setTime(item.getInt(3));
                                        Update(point, type, raceId);
                                    }else{
                                        point = new Point(item.getLong(0),item.getDouble(1), item.getDouble(2), item.getInt(3));
                                        Insert(point, type, raceId, true);
                                    }
                                    p.add(point);
                                }
                            }else
                                p = getPositionsByRace(raceId);
                        } catch (JSONException e) {
                            p = getPositionsByRace(raceId);
                        }
                        if(callback != null)
                            callback.callback(p, changed);
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

    static public void sendToServer(final Context context){
        sendToServer(context, null);
    }

    static public void sendToServer(final Context context, final DataCallback callback){
        JSONObject params = new JSONObject();
        try {
            params.accumulate("table", "positions");

            JSONArray arr = new JSONArray();
            for(Point point: positionsTable.getSyncNew()){
                arr.put(point.toJSON());
            }
            params.accumulate("new",arr);

            arr = new JSONArray();
            for(Point point: positionsTable.getSyncEdit()){
                arr.put(point.toJSON());
            }
            params.accumulate("edit",arr);

            arr = new JSONArray();
            for(Point point: positionsTable.getSyncDel()){
                arr.put(point.toJSON());
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
