package it.univaq.byte_predator.shiptracker.Tables;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import it.univaq.byte_predator.shiptracker.Helper.HelperDatabase;
import it.univaq.byte_predator.shiptracker.Models.Point;

/**
 * Created by byte-predator on 21/02/18.
 */

public class positionsTable {
    static private String TABLE = "positions";

    static public void CREATE(SQLiteDatabase db){
        String sql = "CREATE TABLE "+TABLE+" (" +
                "  Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  Type INTEGER NOT NULL, " +
                "  IdRef INTEGER NOT NULL, " +
                "  Latitude DOUBLE NOT NULL, " +
                "  Longitude DOUBLE NOT NULL, " +
                "  Time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "  Sync BOOLEAN NOT NULL DEFAULT 1, " +
                "  Del BOOLEAN NOT NULL DEFAULT 0" +
                ")";
        db.execSQL(sql);
    }

    static public void UPDATE(SQLiteDatabase db){
        db.execSQL("DROP TABLE "+TABLE+";");
        CREATE(db);
    }

    static public ArrayList<Point> getPositionsByRace(long Id){
        SQLiteDatabase db = HelperDatabase.getInstance().getReadableDatabase();
        ArrayList<Point> points = new ArrayList<Point>();

        return points;
    }
}
