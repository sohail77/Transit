package com.sohail.transit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.google.transit.realtime.GtfsRealtime;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    static URL url;
    LocationManager manager;
    LatLng currentLocation;
    static  GoogleMap myMap;
     Handler repeater;
    MarkerOptions markerOptions=new MarkerOptions();
    static boolean isFirstRun=true;
    static   Marker m;
    final Handler mHandler = new Handler();
    FloatingActionButton fab;
    static Context context;

    static int count=0;
    Marker marker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        context=MainActivity.this;
        fab=findViewById(R.id.myLoc);
        currentLocation=new LatLng(44.648766,-63.575237);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);

        }else{
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, listener);

        }
        try {
            url = new URL("http://gtfs.halifax.ca/realtime/Vehicle/VehiclePositions.pb");
//            new doInBackGround().execute();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MainActivity.this);

        mHandlerTask.run();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(myMap!=null){

                    myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,18.0f));

                }
            }
        });
    }

    final Runnable mHandlerTask = new Runnable()
    {
        @Override
        public void run() {
            new doInBackGround().execute();
            mHandler.postDelayed(mHandlerTask,10000);
//                doSomething();
//                mHandler.postDelayed(mHandlerTask, INTERVAL);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences pref=getSharedPreferences("MapInstance",MODE_PRIVATE);
        LatLng coords=new LatLng(Double.valueOf(pref.getString("latitude","44.648766")),Double.valueOf(pref.getString("longitude","-63.575237")));
        float zoom=pref.getFloat("zoom",18.0f);
        float tilt=pref.getFloat("tilt",90f);

        if(myMap!=null){
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coords,zoom));

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveState();
    }

    public void saveState(){
        if(myMap!=null) {
            CameraPosition cameraPosition = myMap.getCameraPosition();
            LatLng coords=new LatLng(cameraPosition.target.latitude,cameraPosition.target.longitude);
            float zoom=cameraPosition.zoom;
            float tilt=cameraPosition.tilt;

            SharedPreferences pref=getSharedPreferences("MapInstance",MODE_PRIVATE);
            SharedPreferences.Editor editor=pref.edit();
            editor.putString("latitude", String.valueOf(coords.latitude));
            editor.putString("longitude", String.valueOf(coords.longitude));
            editor.putFloat("zoom",zoom);
            editor.putFloat("tilt",tilt);
            editor.commit();
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        myMap=googleMap;

        SharedPreferences pref=getSharedPreferences("MapInstance",MODE_PRIVATE);
        LatLng coords=new LatLng(Double.valueOf(pref.getString("latitude","44.648766")),Double.valueOf(pref.getString("longitude","-63.575237")));
        float zoom=pref.getFloat("zoom",18.0f);
        float tilt=pref.getFloat("tilt",90f);

        if(myMap!=null){
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coords,zoom));

        }

//        googleMap.addMarker(new MarkerOptions().position(currentLocation)
//                .title("your location"));
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
    }

   LocationListener listener= new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            currentLocation=new LatLng(location.getLatitude(),location.getLongitude());

            if(myMap!=null){
                myMap.clear();
                myMap.addMarker(new MarkerOptions().position(currentLocation));
                myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,14.0f));

            }
//            markerOptions

//            marker=myMap.addMarker(markerOptions);

//            marker.setPosition(currentLocation);
//            marker.setTitle("Your location");
//             map.addMarker(new MarkerOptions().position(currentLocation).title("Your Location"));
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    public static class doInBackGround extends AsyncTask<Void, Void, List<GtfsRealtime.FeedEntity>>{


        @Override
        protected List<GtfsRealtime.FeedEntity> doInBackground(Void... voids) {


            try {

                GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(url.openStream());

                return GtfsRealtime.FeedMessage.parseFrom(url.openStream()).getEntityList();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @SuppressLint("MissingPermission")
        @Override
        protected void onPostExecute(List<GtfsRealtime.FeedEntity> feedEntities) {
            super.onPostExecute(feedEntities);
            count++;
            Log.e("counter",count + "");
            myMap.clear();
            for (GtfsRealtime.FeedEntity entity : feedEntities) {
                if (entity.hasVehicle()) {


                    Log.e("Hello",entity.getVehicle() + "");
                    if(entity.getVehicle().hasPosition()){
//                        if(myMap!=null){
//                            myMap.clear();
//                        }
                        if(isFirstRun) {
                            IconGenerator icg = new IconGenerator(context);
//                            icg.setColor(Color.GREEN); // green background
                            icg.setBackground(context.getResources().getDrawable(R.drawable.marker_bus));
                            icg.setTextAppearance(R.style.icon_style); // black text
                            Bitmap bm = icg.makeIcon(entity.getVehicle().getTrip().getRouteId());
                            MarkerOptions options = new MarkerOptions();
                            options.position(new LatLng(entity.getVehicle().getPosition().getLatitude(), entity.getVehicle().getPosition().getLongitude()));
//                            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.bus));
                            options.icon(BitmapDescriptorFactory.fromBitmap(bm));

                            options.title(entity.getVehicle().getTrip().getRouteId());
                            m= myMap.addMarker(options);
                        }else{
                            if(m!=null){
//                                m.remove();
                                IconGenerator icg = new IconGenerator(context);
//                                icg.setColor(Color.GREEN); // green background
                                icg.setBackground(context.getResources().getDrawable(R.drawable.marker_bus));

                                icg.setTextAppearance(R.style.icon_style); // black text
                                Bitmap bm = icg.makeIcon(entity.getVehicle().getTrip().getRouteId());
                                myMap.setMyLocationEnabled(true);
                                MarkerOptions options = new MarkerOptions();
                                options.position(new LatLng(entity.getVehicle().getPosition().getLatitude(), entity.getVehicle().getPosition().getLongitude()));
//                                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.bus));
                                options.icon(BitmapDescriptorFactory.fromBitmap(bm));

                                options.title(entity.getVehicle().getTrip().getRouteId());
                                m= myMap.addMarker(options);
                            }
//                            m.setPosition(new LatLng(entity.getVehicle().getPosition().getLatitude(),entity.getVehicle().getPosition().getLongitude()));
                        }
//                        myMap.addMarker(new MarkerOptions().position(new LatLng(entity.getVehicle().getPosition().getLatitude(),entity.getVehicle().getPosition().getLongitude())).title(entity.getVehicle().getTrip().getRouteId()).infoWindowAnchor(1.0f,1.0f))
//                                .setIcon(BitmapDescriptorFactory.fromResource(R.drawable.bus));

                    }

//                    System.out.println(entity.getTripUpdate());
                }
            }
            isFirstRun=false;

        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, listener);
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                }else {
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


    }
}
