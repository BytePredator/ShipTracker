package it.univaq.byte_predator.shiptracker.Controllers.Main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

import it.univaq.byte_predator.shiptracker.Helper.DataCallback;
import it.univaq.byte_predator.shiptracker.Models.Boa;
import it.univaq.byte_predator.shiptracker.Models.Track;
import it.univaq.byte_predator.shiptracker.R;
import it.univaq.byte_predator.shiptracker.Tables.boasTable;
import it.univaq.byte_predator.shiptracker.Tables.tracksTable;

/**
 * Created by byte-predator on 21/02/18.
 */

public class MainActivity extends AppCompatActivity{
    public static int ADD_TRACK_REQUEST = 1;
    public static int EDIT_TRACK_REQUEST = 2;

    TabAdapter tabs;
    ViewPager pager;
    TabLayout tabLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boasTable.getFromServer(this, new BoaCallback(this));

        setContentView(R.layout.main_layout);

        tabs = new TabAdapter(this, getSupportFragmentManager());

        pager = findViewById(R.id.pager);
        pager.setAdapter(tabs);

        tabLayout = findViewById(R.id.tab);
        tabLayout.setupWithViewPager(pager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.menu_settings:

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.w("main","request: "+requestCode+" result: "+resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ADD_TRACK_REQUEST && resultCode == 1) {
            Toast.makeText(this,"Track aggiunta", Toast.LENGTH_LONG).show();
        }
    }

    class BoaCallback implements DataCallback<Boa>{
        private Context context;

        public BoaCallback(final Context context){
            this.context = context;
        }

        @Override
        public void callback(ArrayList<Boa> data) {
            tracksTable.getFromServer(this.context, new TrackCallback());
        }
    }

    class TrackCallback implements DataCallback<Track>{

        @Override
        public void callback(ArrayList<Track> data) {
            tabs.getTracksFragment().RefreshTracks();
        }
    }
}
