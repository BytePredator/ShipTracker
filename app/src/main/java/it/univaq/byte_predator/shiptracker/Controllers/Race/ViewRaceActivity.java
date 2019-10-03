package it.univaq.byte_predator.shiptracker.Controllers.Race;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.TypeEvaluator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Property;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

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

import it.univaq.byte_predator.shiptracker.Helper.DataCallback;
import it.univaq.byte_predator.shiptracker.Helper.HelperLatLng;
import it.univaq.byte_predator.shiptracker.Helper.HelperTime;
import it.univaq.byte_predator.shiptracker.Helper.LatLngInterpolator;
import it.univaq.byte_predator.shiptracker.Models.Boa;
import it.univaq.byte_predator.shiptracker.Models.Point;
import it.univaq.byte_predator.shiptracker.Models.Race;
import it.univaq.byte_predator.shiptracker.Models.Track;
import it.univaq.byte_predator.shiptracker.Models.Waypoint;
import it.univaq.byte_predator.shiptracker.R;
import it.univaq.byte_predator.shiptracker.Tables.positionsTable;
import it.univaq.byte_predator.shiptracker.Tables.racesTable;

public class ViewRaceActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Track track;
    private Race race;
    private GoogleMap googleMap;
    private Bitmap boa_bitmap_default, boa_bitmap_next, boa_bitmap_done, ship_bitmap;
    private ArrayList<Boa> boas;
    private Marker ship;
    private boolean running = false;
    private Polyline polyline;
    private SeekBar seekBar;
    private TextView current, total;
    private SupportMapFragment mapFragment;
    private RelativeLayout wrapper;
    private ProgressBar progressBar;
    private Button play, pause;
    private ObjectAnimator bar_anim, marker_anim;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.race_view);

        this.mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.race_view_map);
        this.wrapper = (RelativeLayout) findViewById(R.id.control_panel);
        this.progressBar = (ProgressBar) findViewById(R.id.loading_bar);

        long Id = getIntent().getLongExtra("Id", 0);

        this.race = racesTable.getRace(Id);
        if(this.race.getPoints().size() == 0)
            this.loadRace();
        this.track = this.race.track();

        setTitle(this.track.getName());

        this.seekBar = (SeekBar) findViewById(R.id.seekBar);
        this.seekBar.setMax(this.race.getTime());
        this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean touch = false;
            boolean run;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(this.touch) {

                    race.setCurrentTime(i);
                    updateBoasIcons();
                    current.setText(HelperTime.unix2str(i));

                    if(race.getCurrentPoint()+1 == race.getPoints().size()) {
                        Point current = race.getPoint(race.getCurrentPoint()-1);
                        Point next = race.getPoint(race.getCurrentPoint());
                        ship.setPosition(race.getPoint(race.getCurrentPoint()).getLatLng());
                        ship.setRotation((float) HelperLatLng.bearingFrom2Points(current, next));
                    }else {
                        Point current = race.getPoint(race.getCurrentPoint());
                        Point next = race.getPoint(race.getCurrentPoint() + 1);

                        LatLngInterpolator interpolator = new LatLngInterpolator.Spherical();
                        ship.setPosition(interpolator.interpolate(
                                ((float) (i - current.getTime() + race.getPoint(0).getTime())) / (next.getTime() - current.getTime()),
                                current.getLatLng(),
                                next.getLatLng())
                        );
                        ship.setRotation((float) HelperLatLng.bearingFrom2Points(current, next));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                this.touch = true;

                this.run = running;
                pauseRace();
                marker_anim = null;
                bar_anim = null;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                this.touch = false;

                if(this.run)
                    playRace();
            }
        });

        this.current = (TextView) findViewById(R.id.current_time);
        this.total = (TextView) findViewById(R.id.total_time);

        this.total.setText("/"+HelperTime.unix2str(this.race.getTime()));

        this.play = (Button) findViewById(R.id.play_button);
        this.pause = (Button) findViewById(R.id.pause_button);

        this.play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playRace();
            }
        });

        this.pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseRace();
            }
        });

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

        drawable = getResources().getDrawable(R.drawable.ic_navigation_black_24dp);
        ship_bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(ship_bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        this.mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (this.googleMap == null)
            this.googleMap = googleMap;
        this.boas = this.track.getUniqueBoas();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        Marker m = null;
        for (int i = 0; i < this.boas.size(); i++) {
            m = googleMap.addMarker(createBoa(this.boas.get(i)));
            if(i == 0)
                m.setIcon(BitmapDescriptorFactory.fromBitmap(boa_bitmap_done));
            else if(i == 1)
                m.setIcon(BitmapDescriptorFactory.fromBitmap(boa_bitmap_next));
            builder.include(m.getPosition());
            this.boas.get(i).setMarker(m);
        }
        if (this.boas.size() > 1) {
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

        this.setShipMarker();
    }

    private MarkerOptions createBoa(Boa data){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(data.getLatitude(), data.getLongitude()));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(boa_bitmap_default));
        markerOptions.flat(true).anchor(.5f, .5f);
        return markerOptions;
    }

    private void setShipMarker(){

        if (this.googleMap == null)
            return;
        if(this.race.getPoints().size() <= this.race.getCurrentPoint()+1)
            return;

        Point current = this.race.getPoint(this.race.getCurrentPoint());
        Point next = this.race.getPoint(this.race.getCurrentPoint()+1);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(current.getLatitude(), current.getLongitude()));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(this.ship_bitmap));
        markerOptions.flat(true).anchor(.5f, .5f);
        markerOptions.rotation((float) HelperLatLng.bearingFrom2Points(current, next));
        markerOptions.zIndex(10);
        this.ship = this.googleMap.addMarker(markerOptions);
    }

    private void loadRace(){
        getSupportFragmentManager().beginTransaction().hide(this.mapFragment).commit();
        this.wrapper.setVisibility(View.GONE);
        this.progressBar.setVisibility(View.VISIBLE);
        positionsTable.getRaceFromServer(getApplicationContext(), this.race.getId(), new PositionsCallback());
    }

    private void raceLoaded(){
        getSupportFragmentManager().beginTransaction().show(this.mapFragment).commit();
        this.wrapper.setVisibility(View.VISIBLE);
        this.progressBar.setVisibility(View.GONE);
        this.setShipMarker();
    }

    private void playRace(){
        if(this.race.getCurrentPoint()+1 == this.race.getPoints().size()) {
            this.race.setCurrentTime(0);
            this.updateBoasIcons();
            this.ship.setPosition(this.race.getPoint(0).getLatLng());
        }
        this.play.setVisibility(View.GONE);
        this.pause.setVisibility(View.VISIBLE);
        this.running = true;
        this.nextAnimation();
    }

    private void pauseRace(){
        this.play.setVisibility(View.VISIBLE);
        this.pause.setVisibility(View.GONE);
        if(this.bar_anim != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                this.bar_anim.pause();
            }else {
                this.bar_anim.cancel();
            }
        }
        if(this.marker_anim != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                this.marker_anim.pause();
            }else{
                this.bar_anim.cancel();
            }
        }
        this.running = false;
    }

    private void nextAnimation(){
        if (this.googleMap == null)
            return;
        if(this.race.getPoints().size() <= this.race.getCurrentPoint()+1) {
            this.pauseRace();
            return;
        }

        Point current = this.race.getPoint(this.race.getCurrentPoint());
        Point next = this.race.getPoint(this.race.getCurrentPoint()+1);

        if(this.marker_anim == null) {
            //this.ship.setPosition(current.getLatLng());
            this.ship.setRotation((float) HelperLatLng.bearingFrom2Points(current, next));
            this.marker_anim = this.animateMarker(this.ship, next.getLatLng(), new LatLngInterpolator.Spherical(), next.getTime() - this.race.getCurrentTime());
            this.marker_anim.start();
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                this.marker_anim.resume();
            }else{
                this.marker_anim.start();
            }
        }
        if(this.bar_anim == null) {
            int start_time = this.race.getPoint(0).getTime();
            this.bar_anim = this.animateSeekBar(this.seekBar, this.race.getCurrentTime() - start_time, next.getTime()- start_time, next.getTime() - this.race.getCurrentTime());
            this.bar_anim.start();
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                this.bar_anim.resume();
            }else{
                this.bar_anim.start();
            }
        }
    }

    private void endAnimation(){
        int w = this.race.getCurrentWaypoint();
        this.race.nextPoint();
        if(w != this.race.getCurrentWaypoint())
            this.updateBoasIcons();

        this.marker_anim = null;
        this.bar_anim = null;
        this.nextAnimation();
    }

    private void updateBoasIcons(){
        ArrayList<Boa> completed = this.race.completedBoas();
        Boa next = this.race.nextBoa();

        for (int i = 0; i < this.boas.size(); i++){
            Boa boa = this.boas.get(i);
            Marker m = this.boas.get(i).getMarker();
            if(isInList(completed, boa)){
                m.setIcon(BitmapDescriptorFactory.fromBitmap(boa_bitmap_done));
            }else if( next != null && next.getId() == boa.getId()) {
                m.setIcon(BitmapDescriptorFactory.fromBitmap(boa_bitmap_next));
            }else{
                m.setIcon(BitmapDescriptorFactory.fromBitmap(boa_bitmap_default));
            }
        }
    }

    private boolean isInList(ArrayList<Boa> list, Boa boa){
        for (Boa b : list){
            if(boa.getId() == b.getId())
                return true;
        }
        return  false;
    }

    private ObjectAnimator animateMarker(final Marker marker, final LatLng finalPosition, final LatLngInterpolator latLngInterpolator, long duration) {
        TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
            @Override
            public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
                LatLng p = latLngInterpolator.interpolate(fraction, startValue, endValue);
                if(fraction != 1)
                    marker.setRotation((float) HelperLatLng.bearingFrom2LatLng(p, endValue));
                return p;
            }
        };
        Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
        ObjectAnimator animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition);
        animator.setDuration(duration*1000);
        animator.setInterpolator(new TimeInterpolator() {
            @Override
            public float getInterpolation(float v) {
                return v;
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                endAnimation();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        return animator;
    }

    private ObjectAnimator animateSeekBar(SeekBar seekBar, final int start, int stop, final long duration) {
        ObjectAnimator animator = ObjectAnimator.ofInt(seekBar, "progress", start, stop);
        animator.setDuration(duration*1000);
        animator.setInterpolator(new TimeInterpolator() {
            @Override
            public float getInterpolation(float v) {
                current.setText(HelperTime.unix2str((int) (start+v*duration)));
                return v;
            }
        });
        return animator;
    }

    class PositionsCallback  implements DataCallback<Point> {
        @Override
        public void callback(ArrayList<Point> data) {
            race.setPoints(data);
            raceLoaded();
        }
    }
}
