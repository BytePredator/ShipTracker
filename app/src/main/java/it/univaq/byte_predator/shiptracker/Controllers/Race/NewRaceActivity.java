package it.univaq.byte_predator.shiptracker.Controllers.Race;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

import it.univaq.byte_predator.shiptracker.Helper.HelperLatLng;
import it.univaq.byte_predator.shiptracker.Helper.HelperTime;
import it.univaq.byte_predator.shiptracker.Models.Boa;
import it.univaq.byte_predator.shiptracker.Models.Point;
import it.univaq.byte_predator.shiptracker.Models.Race;
import it.univaq.byte_predator.shiptracker.Models.Track;
import it.univaq.byte_predator.shiptracker.Models.Waypoint;
import it.univaq.byte_predator.shiptracker.R;
import it.univaq.byte_predator.shiptracker.Tables.positionsTable;
import it.univaq.byte_predator.shiptracker.Tables.racesTable;
import it.univaq.byte_predator.shiptracker.Tables.tracksTable;

public class NewRaceActivity
        extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback {

    private static final int REQUEST_CODE = 1;
    private static final long UPDATE_INTERVAL = 300;
    private static final long FASTEST_INTERVAL = 100;
    private GoogleApiClient client;
    private LocationCallback locationCallback;
    private ArrayList<Boa> boas;
    private Track track;
    private Race race;
    private GoogleMap googleMap;
    private Location location, old_location;
    private SupportMapFragment mapFragment;
    private Bitmap boa_bitmap_default, boa_bitmap_next, boa_bitmap_done, ship_bitmap;
    private Polyline polyline;
    private Marker ship;
    private TextView current;
    private Button play, stop;
    private boolean running = false;
    private long start_time;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.race_new);


        long Id = getIntent().getLongExtra("Id", 0);
        this.track = tracksTable.getTrack(Id);
        this.race = new Race(0, 0, new ArrayList<Point>());
        this.race.setWaypoints(this.track.getWaypoints());
        this.current = (TextView) findViewById(R.id.current_time);
        this.play = (Button) findViewById(R.id.play_button);
        this.stop = (Button) findViewById(R.id.stop_button);

        this.play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRace();
            }
        });
        this.stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRace();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<>();
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

            if(permissions.size()>0){
                requestPermissions(permissions.toArray(new String[permissions.size()]), REQUEST_CODE);
            }
        }

        if(GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext()) != ConnectionResult.SUCCESS)
            finish();
        client = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();

        this.mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.race_new_map);

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
    protected void onStart() {
        super.onStart();
        if(client != null)
            client.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(client != null && client.isConnected()) {
            if(this.locationCallback != null)
                LocationServices.getFusedLocationProviderClient(getApplicationContext()).removeLocationUpdates(this.locationCallback);
            client.disconnect();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE){
            for( int i=0; i < grantResults.length; i++){
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    finish();
                    return;
                }
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        locationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if(location != null) {
                    newLocation(location);
                    updateMap();
                }
            }
        });

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        LocationSettingsRequest settings = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build();

        LocationServices.getSettingsClient(getApplicationContext()).checkLocationSettings(settings)
            .addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                @Override
                public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                    try{
                        LocationSettingsResponse response = task.getResult(ApiException.class);
                    }catch (ApiException e) {
                        e.printStackTrace();
                    }
                }
            });

        this.locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location location = locationResult.getLastLocation();
                //Log.w("new race","result: "+location.getLatitude() + ", " + location.getLongitude());
                newLocation(location);
                updateMap();
            }
        };


        FusedLocationProviderClient fusedClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        fusedClient.requestLocationUpdates(locationRequest, this.locationCallback, Looper.myLooper());
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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

        this.updateMap();
    }

    private MarkerOptions createBoa(Boa data){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(data.getLatitude(), data.getLongitude()));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(boa_bitmap_default));
        markerOptions.flat(true).anchor(.5f, .5f);
        return markerOptions;
    }

    private void updateMap(){
        if(this.old_location != null && this.location != null && this.ship != null) {
            Point current = new Point(0, this.location.getLatitude(), this.location.getLongitude());
            Point old = new Point(0, this.old_location.getLatitude(), this.old_location.getLongitude());

            this.ship.setPosition(current.getLatLng());
            if(old.getLatitude() != current.getLatitude() || old.getLongitude() != current.getLongitude())
                this.ship.setRotation((float) HelperLatLng.bearingFrom2Points(old, current));
        }

        this.current.setText(HelperTime.unix2str(this.race.getTime()));

        if(this.running)
            this.updateBoasIcons();
        if(this.race.getCurrentWaypoint() >= this.track.getWaypoints().size())
            endRace();
    }

    private void newLocation(Location l){
        this.old_location = this.location;
        this.location = l;

        if(this.ship == null)
            this.setShipMarker();

        addPoint(location);
    }

    private void setShipMarker(){

        if (this.googleMap == null)
            return;
        if(this.location == null || this.old_location == null)
            return;
        Point current = new Point(0, this.location.getLatitude(), this.location.getLongitude());
        Point old = new Point(0, this.old_location.getLatitude(), this.old_location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(current.getLatitude(), current.getLongitude()));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(this.ship_bitmap));
        markerOptions.flat(true).anchor(.5f, .5f);
        if(old.getLatitude() != current.getLatitude() || old.getLongitude() != current.getLongitude())
            markerOptions.rotation((float) HelperLatLng.bearingFrom2Points(old, current));
        markerOptions.zIndex(10);
        this.ship = this.googleMap.addMarker(markerOptions);
    }

    private void updateBoasIcons(){
        ArrayList<Boa> completed = this.race.completedBoas();
        Boa next = this.race.nextBoa();

        for (int i = 0; i < this.boas.size(); i++){
            Boa boa = this.boas.get(i);
            boolean f = false;
            Marker m = this.boas.get(i).getMarker();
            ArrayList<Waypoint> waypoints = this.track.getWaypoints();
            int curr_waypoint = this.race.getCurrentWaypoint();
            for(int j=0; j < waypoints.size() && j < curr_waypoint; j++){
                if(waypoints.get(j).getBoa().getId() == boa.getId()) {
                    m.setIcon(BitmapDescriptorFactory.fromBitmap(boa_bitmap_done));
                    f = true;
                    break;
                }
            }
            if( curr_waypoint < waypoints.size() && waypoints.get(curr_waypoint).getBoa().getId() == boa.getId()) {
                m.setIcon(BitmapDescriptorFactory.fromBitmap(boa_bitmap_next));
                f = true;
            }
            if(!f){
                m.setIcon(BitmapDescriptorFactory.fromBitmap(boa_bitmap_default));
            }
        }
    }

    private boolean inStartPosition(){
        if(this.track.getWaypoints().size() == 0)
            return false;
        return this.track.getWaypoint(0).getBoa().inRange(this.location);
    }

    private void startRace(){
        if(!this.running && inStartPosition()){
            this.race.setId(racesTable.Save(this.race, this.track.getId(), true));
            this.track.addRace(this.race);
            tracksTable.Save(this.track, true);
            this.start_time = System.currentTimeMillis()/1000;
            this.play.setVisibility(View.GONE);
            this.stop.setVisibility(View.VISIBLE);
            this.running = true;
        }
    }

    private void stopRace(){
        if(this.running){
            this.running = false;
            racesTable.Delete(this.race.getId());
            Log.w("new race", "stopped");
        }
    }

    private void endRace(){
        racesTable.Save(this.race,this.track.getId(), true);
        this.running = false;
        if(client != null && client.isConnected()) {
            if(this.locationCallback != null)
                LocationServices.getFusedLocationProviderClient(getApplicationContext()).removeLocationUpdates(this.locationCallback);
        }
        finish();
    }

    private void addPoint(Location location){
        if(!this.running)
            return;

        int time = (int) (System.currentTimeMillis()/1000-this.start_time);
        Point p = new Point(0, location.getLatitude(), location.getLongitude(), time);
        p.setId(positionsTable.Insert(p, 1, this.race.getId()));
        this.race.addPoint(p);
    }
}
