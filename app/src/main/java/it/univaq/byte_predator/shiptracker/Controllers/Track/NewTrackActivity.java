package it.univaq.byte_predator.shiptracker.Controllers.Track;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import it.univaq.byte_predator.shiptracker.Models.Boa;
import it.univaq.byte_predator.shiptracker.Models.Track;
import it.univaq.byte_predator.shiptracker.Models.Waypoint;
import it.univaq.byte_predator.shiptracker.R;
import it.univaq.byte_predator.shiptracker.Tables.boasTable;
import it.univaq.byte_predator.shiptracker.Tables.tracksTable;


/**
 * Created by byte-predator on 22/02/18.
 */

public class NewTrackActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private ArrayList<Boa> boas;
    private Track track;
    private Bitmap boa_bitmap_default, boa_bitmap_selected, boa_bitmap_last;
    private GoogleMap googleMap;
    private Polyline polyline;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        track = new Track(0,getIntent().getStringExtra("Name"));

        setContentView(R.layout.track_new);
        setTitle(getIntent().getStringExtra("Name"));

        Drawable drawable = getResources().getDrawable(R.drawable.default_boa_marker);
        boa_bitmap_default = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(boa_bitmap_default);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);


        drawable = getResources().getDrawable(R.drawable.selected_boa_marker);
        boa_bitmap_selected = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(boa_bitmap_selected);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);


        drawable = getResources().getDrawable(R.drawable.last_boa_marker);
        boa_bitmap_last = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(boa_bitmap_last);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.track_new_map);
        mapFragment.getMapAsync(this);

        findViewById(R.id.save_track).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                long id = tracksTable.Insert(track);
                Log.w("save", String.valueOf(id));
                Intent returnIntent = new Intent();
                setResult(1,returnIntent);
                NewTrackActivity.super.finish();
            }
        });
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        if (this.googleMap == null)
            this.googleMap = googleMap;
        boas = boasTable.getBoas();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        Marker m = null;
        for (int i = 0; i < boas.size(); i++) {
            m = googleMap.addMarker(createBoa(boas.get(i), false));
            builder.include(m.getPosition());
            boas.get(i).setMarker(m);
        }
        if (boas.size() > 1) {
            LatLngBounds bounds = builder.build();
            android.graphics.Point p = new android.graphics.Point();
            getWindowManager().getDefaultDisplay().getSize(p);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, p.x, p.y, 200));
        }else if(m != null)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(m.getPosition(), 12f));
        googleMap.setOnMarkerClickListener(this);
    }

    private MarkerOptions createBoa(Boa data, boolean selected){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(data.getLatitude(), data.getLongitude()));
        if(selected)
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(boa_bitmap_selected));
        else
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(boa_bitmap_default));
        markerOptions.flat(true).anchor(.5f, .5f);
        return markerOptions;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        boolean changed = false;
        for(int i=0; i < boas.size(); i++){
            if(marker.equals(boas.get(i).getMarker())){
                if(track.waypointsNumber()>0 && track.getWaypoint(track.waypointsNumber()-1).getBoa().getId()==boas.get(i).getId()){
                    //Elimina l'ultimo waypoint
                    track.removeWaypoint(track.waypointsNumber()-1);

                    int counter = 0;
                    for(int j = 0; j < track.waypointsNumber(); j++)
                        if(track.getWaypoint(j).getBoa().getId()==boas.get(i).getId()) {
                            counter++;          //Conto se è un waypoint multiplo
                            break;
                        }
                    if(counter==0)
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(boa_bitmap_default));
                    else
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(boa_bitmap_selected));

                    if(track.waypointsNumber()>0)
                        for(int j = 0; j < boas.size(); j++)
                            if(track.getWaypoint(track.waypointsNumber()-1).getBoa().getId() == boas.get(j).getId()){
                                boas.get(j).getMarker().setIcon(BitmapDescriptorFactory.fromBitmap(boa_bitmap_last));
                                break;              //Cambio l'icona del penultimo waypoint
                            }

                }else{      //Aggiungo un waypoint
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(boa_bitmap_last));
                    Waypoint w = new Waypoint(boas.get(i),track.waypointsNumber()+1);
                    track.addWaypoint(w);

                    if(track.waypointsNumber()>1)
                        for(int j = 0; j < boas.size(); j++)
                            if(track.getWaypoint(track.waypointsNumber()-2).getBoa().getId() == boas.get(j).getId()){
                                boas.get(j).getMarker().setIcon(BitmapDescriptorFactory.fromBitmap(boa_bitmap_selected));
                                break;              //Cambio l'icona del penultimo waypoint
                            }
                }
                changed = true;
                break;
            }
        }
        if(changed){
            if(polyline != null)
                polyline.remove();
            PolylineOptions polylineOptions = new PolylineOptions();
            for(int i = 0; i < track.waypointsNumber(); i++){
                /*Boa boa = null;
                for(Boa b: boas){
                    if(b.getId() == track.getWaypoint(i).getBoa().getId())
                        boa = b;
                }
                if(boa != null)
                    polylineOptions.add(boa.getLatLng());*/
                polylineOptions.add(track.getWaypoint(i).getBoa().getLatLng());
            }
            polylineOptions.width(4f);
            //polyline.setEndCap(new ButtCap());
            //polyline.setStartCap(new RoundCap());
            polyline = googleMap.addPolyline(polylineOptions);
        }
        return true;
    }
}
