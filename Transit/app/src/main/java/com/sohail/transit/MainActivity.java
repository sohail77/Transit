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
import android.support.design.button.MaterialButton;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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
import com.sohail.transit.Adapters.BusAdapter;
import com.sohail.transit.Models.RoutesModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
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
    ArrayList<RoutesModel> routes=new ArrayList<>();
    static ArrayList<String> wantedRoutes=new ArrayList<>();
    MaterialButton filterBtn;
    RecyclerView busRv;
    BusAdapter adapter;
    static int count=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        context=MainActivity.this;
        busRv=findViewById(R.id.busRv);
        busRv.setLayoutManager(new LinearLayoutManager(this));
        filterBtn=findViewById(R.id.filterBtn);
        fab=findViewById(R.id.myLoc);
        currentLocation=new LatLng(44.648766,-63.575237);
        getRoutes();
        adapter=new BusAdapter(MainActivity.this,routes);
        busRv.setAdapter(adapter);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);

        }else{
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, listener);

        }
        try {
            url = new URL("http://gtfs.halifax.ca/realtime/Vehicle/VehiclePositions.pb");
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

        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wantedRoutes=adapter.giveMeRoutes();
                if(wantedRoutes.size()==0){
                    Toast.makeText(MainActivity.this,"Please select atleast one item",Toast.LENGTH_LONG).show();
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
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences pref=getSharedPreferences("MapInstance",MODE_PRIVATE);
        LatLng coords=new LatLng(Double.valueOf(pref.getString("latitude","44.648766")),Double.valueOf(pref.getString("longitude","-63.575237")));
        float zoom=pref.getFloat("zoom",18.0f);

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
            editor.apply();
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
                    if(wantedRoutes.size()>0) {
                        if(wantedRoutes.contains(entity.getVehicle().getTrip().getRouteId())){
                            makeMarkers(entity);
                        }
                    }else{
                        makeMarkers(entity);
                    }
                    Log.e("Hello",entity.getVehicle() + "");

                }
            }

        }
    }

    public static  void makeMarkers(GtfsRealtime.FeedEntity entity){
        if(entity.getVehicle().hasPosition()){

            IconGenerator icg = new IconGenerator(context);
            icg.setBackground(context.getResources().getDrawable(R.drawable.marker_bus));
            icg.setTextAppearance(R.style.icon_style); // black text
            Bitmap bm = icg.makeIcon(entity.getVehicle().getTrip().getRouteId());
            MarkerOptions options = new MarkerOptions();
            options.position(new LatLng(entity.getVehicle().getPosition().getLatitude(), entity.getVehicle().getPosition().getLongitude()));
            options.icon(BitmapDescriptorFactory.fromBitmap(bm));
            options.title(entity.getVehicle().getTrip().getRouteId());
            m= myMap.addMarker(options);
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

    public void getRoutes(){

        InputStream inputStream = getResources().openRawResource(R.raw.routes);
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(inputStream, Charset.forName("UTF-8")));
        String line = "";
        try {
            while ((line = bufferedReader.readLine()) != null) {
                String[] data = line.split(",");

                RoutesModel routesModel = new RoutesModel();
                routesModel.setRouteId(data[0]);
                routesModel.setName(data[1]);
                routes.add(routesModel);
            }

        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

}
