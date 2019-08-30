package it.univaq.byte_predator.shiptracker.Controllers.Race;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Property;
import android.view.MenuItem;
import android.widget.SeekBar;

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

import it.univaq.byte_predator.shiptracker.Helper.LatLngInterpolator;
import it.univaq.byte_predator.shiptracker.Models.Boa;
import it.univaq.byte_predator.shiptracker.Models.Race;
import it.univaq.byte_predator.shiptracker.Models.Track;
import it.univaq.byte_predator.shiptracker.Models.Waypoint;
import it.univaq.byte_predator.shiptracker.R;
import it.univaq.byte_predator.shiptracker.Tables.racesTable;

public class ViewRaceActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Track track;
    private Race race;
    private GoogleMap googleMap;
    private Bitmap boa_bitmap_default, boa_bitmap_next, boa_bitmap_done;
    private Polyline polyline;
    private SeekBar seekBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.race_view);

        long Id = getIntent().getLongExtra("Id", 0);

        this.race = racesTable.getRace(Id);
        this.track = this.race.track();

        setTitle(this.track.getName());

        Drawable drawable = getResources().getDrawable(R.drawable.default_boa_marker);
        boa_bitmap_default = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(boa_bitmap_default);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        drawable = getResources().getDrawable(R.drawable.last_boa_marker);
        boa_bitmap_next = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(boa_bitmap_next);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        drawable = getResources().getDrawable(R.drawable.selected_boa_marker);
        boa_bitmap_done = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(boa_bitmap_done);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.race_view_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (this.googleMap == null)
            this.googleMap = googleMap;
        ArrayList<Boa> boas = this.track.getUniqueBoas();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        Marker m = null;
        for (int i = 0; i < boas.size(); i++) {
            m = googleMap.addMarker(createBoa(boas.get(i)));
            if(i == 0)
                m.setIcon(BitmapDescriptorFactory.fromBitmap(boa_bitmap_next));
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
        for(Waypoint waypoint: this.track.getWaypoints())
            polylineOptions.add(waypoint.getBoa().getLatLng());
        polylineOptions.width(4f);
        this.polyline = googleMap.addPolyline(polylineOptions);
    }

    private MarkerOptions createBoa(Boa data){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(data.getLatitude(), data.getLongitude()));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(boa_bitmap_default));
        markerOptions.flat(true).anchor(.5f, .5f);
        return markerOptions;
    }

    static private void animateMarker(Marker marker, LatLng finalPosition, final LatLngInterpolator latLngInterpolator, long duration) {
        TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
            @Override
            public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
                return latLngInterpolator.interpolate(fraction, startValue, endValue);
            }
        };
        Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
        ObjectAnimator animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition);
        animator.setDuration(duration);
        animator.start();
    }

    static private void animateSeekBar(SeekBar seekBar, int start, int stop, long duration) {
        ObjectAnimator animator = ObjectAnimator.ofInt(seekBar, "progress", start, stop);
        animator.setDuration(duration);
        animator.start();
    }
}
