package it.univaq.byte_predator.shiptracker.Controllers.Track;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import it.univaq.byte_predator.shiptracker.Controllers.Race.NewRaceActivity;
import it.univaq.byte_predator.shiptracker.Helper.HelperDatabase;
import it.univaq.byte_predator.shiptracker.Models.Boa;
import it.univaq.byte_predator.shiptracker.Models.Race;
import it.univaq.byte_predator.shiptracker.R;
import it.univaq.byte_predator.shiptracker.Models.Track;
import it.univaq.byte_predator.shiptracker.Tables.racesTable;
import it.univaq.byte_predator.shiptracker.Tables.tracksTable;

/**
 * Created by byte-predator on 21/02/18.
 */

public class ViewTrackActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static int ADD_RACE_REQUEST = 1;

    private Track track;
    private Bitmap boa_bitmap_first, boa_bitmap_default;
    private GoogleMap googleMap;
    private ArrayList<Race> data_races;
    private RacesAdapter races;
    private Polyline polyline;
    private AlertDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.track_view);



        long Id = getIntent().getLongExtra("Id", 0);
        if(Id == 0){
            Log.w("view track error", String.valueOf(Id));
            Intent returnIntent = new Intent();
            setResult(1,returnIntent);
            ViewTrackActivity.super.finish();
            return;
        }

        this.track = tracksTable.getTrack(Id);

        setTitle(this.track.getName());

        Drawable drawable = getResources().getDrawable(R.drawable.default_boa_marker);
        boa_bitmap_default = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(boa_bitmap_default);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        drawable = getResources().getDrawable(R.drawable.last_boa_marker);
        boa_bitmap_first = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(boa_bitmap_first);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.track_view_map);
        mapFragment.getMapAsync(this);

        data_races = racesTable.getRaces(this.track.getId());
        races = new RacesAdapter(data_races, this.AMcallback);

        RecyclerView races_list = findViewById(R.id.races_list);
        races_list.setLayoutManager(new LinearLayoutManager(this));
        races_list.setAdapter(races);

        if(data_races.size()>0)
            ((FloatingActionButton)findViewById(R.id.edit_track)).hide();

        findViewById(R.id.add_race).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewTrackActivity.this, NewRaceActivity.class);
                intent.putExtra("Id", track.getId());
                startActivityForResult(intent , ViewTrackActivity.ADD_RACE_REQUEST);
            }
        });




        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_races_dialog);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which){
                for(Long Id: races.getSelected()){
                    racesTable.DeleteRace(Id);
                }
                races.setData(racesTable.getRaces(track.getId()));
                races.getActionMode().finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which){
                races.getActionMode().finish();
            }
        });
        this.dialog = builder.create();
    }


    private ActionMode.Callback AMcallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu){
            mode.getMenuInflater().inflate(R.menu.selection_menu,menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu){return false;}

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item){
            if(item.getItemId() == R.id.menu_delete)
                dialog.show();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode){
            races.selectionStop();
        }
    };


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        if (this.googleMap == null)
            this.googleMap = googleMap;
        ArrayList<Boa> boas = this.track.getUniqueBoas();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        Marker m = null;
        for (int i = 0; i < boas.size(); i++) {
            m = googleMap.addMarker(createBoa(boas.get(i)));
            if(i == 0)
                m.setIcon(BitmapDescriptorFactory.fromBitmap(boa_bitmap_first));
            builder.include(m.getPosition());
            boas.get(i).setMarker(m);
        }
        if (boas.size() > 1) {
            LatLngBounds bounds = builder.build();
            android.graphics.Point p = new android.graphics.Point();
            getWindowManager().getDefaultDisplay().getSize(p);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, p.x, p.y/2, 200));
        }else if(m != null)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(m.getPosition(), 12f));

        PolylineOptions polylineOptions = new PolylineOptions();
        for(Boa boa: track.getBoas())
            polylineOptions.add(boa.getLatLng());
        polylineOptions.width(4f);
        polyline = googleMap.addPolyline(polylineOptions);
    }



    private MarkerOptions createBoa(Boa data){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(data.getLatitude(), data.getLongitude()));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(boa_bitmap_default));
        markerOptions.flat(true).anchor(.5f, .5f);
        return markerOptions;
    }
}
