package it.univaq.byte_predator.shiptracker.Helper;

import android.app.IntentService;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import it.univaq.byte_predator.shiptracker.Models.Track;
import it.univaq.byte_predator.shiptracker.Tables.tracksTable;

/**
 * Created by byte-predator on 26/02/18.
 */

public class HelperService extends IntentService {

    private static final String NAME = HelperService.class.getSimpleName();

    public HelperService(){
        super(NAME);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SQLiteDatabase db = HelperDatabase.getInstance().getWritableDatabase();
        ArrayList<Track> tracks = tracksTable.getSync();
    }
}
