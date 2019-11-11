package it.univaq.byte_predator.shiptracker.Services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import it.univaq.byte_predator.shiptracker.Helper.DataCallback;
import it.univaq.byte_predator.shiptracker.Models.Boa;
import it.univaq.byte_predator.shiptracker.Models.Point;
import it.univaq.byte_predator.shiptracker.Models.Track;
import it.univaq.byte_predator.shiptracker.Tables.boasTable;
import it.univaq.byte_predator.shiptracker.Tables.positionsTable;
import it.univaq.byte_predator.shiptracker.Tables.tracksTable;

public class SyncService extends IntentService {
    private static final String NAME = SyncService.class.getSimpleName();
    private static int delay = 1000*10;
    private static Timer timer;

    public static final String SYNC_ACTION = "it.univaq.byte_predator.shiptracker.SYNC";
    public static final int BOAS = 1;
    public static final int TRACKS = 2;
    public static final int TRACES = 3;
    public static final int RACES = 4;
    public static final int WAIPOINTS = 5;
    public static final int POINTS = 6;

    public SyncService(){
        super(NAME);
    }

    public void sync(){
        Log.w("syncService","sync");
        this.pushAll();
        //this.pullAll();
    }

    public void pushAll(){
        ArrayList<Track> tracks = tracksTable.getSync();
        ArrayList<Point> points = positionsTable.getSync();
        if(tracks.size()>0)
            tracksTable.sendToServer(getApplicationContext(), new TrackSyncCallback());
        //else if(points.size()>0)
         //   positionsTable.sendToServer(getApplicationContext());
        else
            this.pullAll();
    }

    public void pullAll(){
        boasTable.getFromServer(getApplicationContext(), new BoaCallback());
        tracksTable.getFromServer(getApplicationContext(), new TrackCallback());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String action = intent.getStringExtra("action");

        switch (action){
            case "init":
            default:
                break;
        }


        if(this.timer == null){
            this.timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    sync();
                }
            };
            this.timer.schedule(task, 0, this.delay);
        }
    }

    class BoaCallback implements DataCallback<Boa> {

        @Override
        public void callback(ArrayList<Boa> data, boolean changed) {
            if(changed) {
                Intent broadcastIntent = new Intent(SyncService.SYNC_ACTION);
                broadcastIntent.putExtra("table", SyncService.BOAS);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);
            }
        }
    }

    class TrackCallback implements DataCallback<Track>{

        @Override
        public void callback(ArrayList<Track> data, boolean changed) {
            if(changed) {
                Intent broadcastIntent = new Intent(SyncService.SYNC_ACTION);
                broadcastIntent.putExtra("table", SyncService.TRACKS);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);
            }
        }
    }

    class TrackSyncCallback implements DataCallback<JSONObject>{

        @Override
        public void callback(ArrayList<JSONObject> res, boolean changed) {
            try {
                if(res.size()>0 && res.get(0).has("dbg"))
                    Log.w("sync tracks", res.get(0).getString("dbg").toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pullAll();
            /*if(changed) {
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(SyncService.SYNC_ACTION);
                broadcastIntent.putExtra("table", SyncService.TRACKS);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);
            }*/
        }
    }

}
