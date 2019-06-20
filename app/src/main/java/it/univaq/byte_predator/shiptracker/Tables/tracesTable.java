package it.univaq.byte_predator.shiptracker.Tables;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by byte-predator on 21/02/18.
 */

public class tracesTable {
    static public String TABLE = "traces";

    static public void CREATE(SQLiteDatabase db){
        String sql = "CREATE TABLE "+TABLE+" (" +
                "  Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  Name varchar(20) NOT NULL," +
                "  Sync BOOLEAN NOT NULL DEFAULT 1, " +
                "  Del BOOLEAN NOT NULL DEFAULT 0" +
                ")";
        db.execSQL(sql);
    }

    static public void UPDATE(SQLiteDatabase db){
        db.execSQL("DROP TABLE "+TABLE+";");
        CREATE(db);
    }
}
