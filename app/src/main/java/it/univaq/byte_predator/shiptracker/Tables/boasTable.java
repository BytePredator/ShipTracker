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

/**
 * Created by byte-predator on 21/02/18.
 */

public class boasTable {
    static public String TABLE = "boas";
    static public String ID = "Id";
    static public String LATITUDE = "Latitude";
    static public String LONGITUDE = "Longitude";


    static public void CREATE(SQLiteDatabase db){
        String sql = "CREATE TABLE "+TABLE+" (" +
                "  Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  Latitude DOUBLE NOT NULL," +
                "  Longitude DOUBLE NOT NULL" +
                ")";
        db.execSQL(sql);
    }

    static public void UPDATE(SQLiteDatabase db){
        db.execSQL("DROP TABLE "+TABLE+";");
        CREATE(db);
    }

    static public Boa getBoa(long Id){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        Boa r = null;
        Cursor cursor = db.query(TABLE, new String[]{ID, LATITUDE, LONGITUDE}, ID+"=?", new String[]{String.valueOf(Id)}, null, null, null, null);
        while (cursor.moveToNext()){
            r = new Boa(cursor.getInt(0),
                    cursor.getDouble(1),
                    cursor.getDouble(2));
        }
        return r;
    }

    static public ArrayList<Boa> getBoas(){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        ArrayList<Boa> r = new ArrayList<>();
        Cursor cursor = db.query(TABLE, new String[]{ID, LATITUDE, LONGITUDE}, null, null, null, null, ID);
        while (cursor.moveToNext()){
            r.add(new Boa(cursor.getInt(0),
                    cursor.getDouble(1),
                    cursor.getDouble(2)));
        }
        return r;
    }

    static public long UpdateBoa(Boa data){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ID, data.getId());
        values.put(LATITUDE, data.getLatitude());
        values.put(LONGITUDE, data.getLongitude());
        return db.update(TABLE, values, ID+" = ?", new String[]{String.valueOf(data.getId())});
    }

    static private long InsertBoa(Boa data){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ID, data.getId());
        values.put(LATITUDE, data.getLatitude());
        values.put(LONGITUDE, data.getLongitude());
        return db.insert(TABLE, null, values);
    }

    static private int DeleteBoa(int Id){
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        return db.delete(TABLE, ID+"= ?", new String[]{String.valueOf(Id)});
    }

    static public void getFromServer(final Context context){
        getFromServer(context, null);
    }

    static public void getFromServer(final Context context, final DataCallback callback){
        HelperHTTP.getInstance(context).RequestJSONObject("http://10.10.0.49/get.php?table=boas", "GET",
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ArrayList<Boa> r = new ArrayList<>();
                        try {
                            if(response.getInt("error")==0){
                                JSONArray array = response.getJSONArray("data");
                                for (int i=0; i<array.length(); i++) {
                                    JSONArray item = array.getJSONArray(i);
                                    Boa p;
                                    if((p = getBoa(item.getLong(0)))!=null){
                                        p.setLatitude(item.getDouble(1));
                                        p.setLongitude(item.getDouble(2));
                                        UpdateBoa(p);
                                        Log.w("boaModel", "update: "+String.valueOf(p.getId()));
                                    }else{
                                        p = new Boa(item.getLong(0),
                                                item.getDouble(1),
                                                item.getDouble(2));
                                        InsertBoa(p);
                                        Log.w("boaModel", "insert: "+String.valueOf(p.getId()));
                                    }
                                    r.add(p);
                                }
                            }else
                                r = getBoas();
                        } catch (JSONException e) {
                            r = getBoas();
                        }
                        if(callback != null)
                            callback.callback(r);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(callback != null)
                            callback.callback(getBoas());
                    }
                }
        );
    }
}
