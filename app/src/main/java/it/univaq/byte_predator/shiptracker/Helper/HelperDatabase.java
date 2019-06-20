package it.univaq.byte_predator.shiptracker.Helper;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import it.univaq.byte_predator.shiptracker.Tables.boasTable;
import it.univaq.byte_predator.shiptracker.Tables.positionsTable;
import it.univaq.byte_predator.shiptracker.Tables.racesTable;
import it.univaq.byte_predator.shiptracker.Tables.tracesTable;
import it.univaq.byte_predator.shiptracker.Tables.tracksTable;
import it.univaq.byte_predator.shiptracker.Tables.waypointsTable;

/**
 * Created by byte-predator on 21/02/18.
 */

public class HelperDatabase extends SQLiteOpenHelper {
    private final static String DATABSE = "ship_tracker.db";
    private final static int VERSION = 1;

    private static HelperDatabase instance;

    public static HelperDatabase getInstance(){
        if(instance == null) {
            instance = new HelperDatabase(HelperAC.getContext());
        }
        return instance;
    }

    private HelperDatabase(Context context){
        super(context, DATABSE, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        boasTable.CREATE(db);
        positionsTable.CREATE(db);
        tracesTable.CREATE(db);
        tracksTable.CREATE(db);
        racesTable.CREATE(db);
        waypointsTable.CREATE(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        boasTable.UPDATE(db);
        positionsTable.UPDATE(db);
        tracesTable.UPDATE(db);
        tracksTable.UPDATE(db);
        racesTable.UPDATE(db);
        waypointsTable.UPDATE(db);
    }
}
