package com.sohail.transit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.ui.IconGenerator;
import com.google.transit.realtime.GtfsRealtime;
import com.sohail.transit.Adapters.BusAdapter;
import com.sohail.transit.Adapters.MarkerInfoAdapter;
import com.sohail.transit.Models.InfoWindowModel;
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
    static GoogleMap myMap;
    static Marker m;
    final Handler mHandler = new Handler();
    FloatingActionButton fab;
    static Context context;
    ArrayList<RoutesModel> routes = new ArrayList<>();
    static ArrayList<String> wantedRoutes = new ArrayList<>();
    MaterialButton filterBtn;
    RecyclerView busRv;
    boolean isFirstRunComplete;
    BusAdapter adapter;
    static  boolean isPermissionGranted=false;
    private LocationRequest locationRequest;
    BottomSheetBehavior bottomSheetBehavior;
    LinearLayout bottomSheet;
    FusedLocationProviderClient locationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setting up the location providers
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        SharedPreferences pref = getSharedPreferences("MapInstance", MODE_PRIVATE);
        isFirstRunComplete=pref.getBoolean("isFirstRunComplete",false);

        //Setting up all the views
        context = MainActivity.this;
        busRv = findViewById(R.id.busRv);
        busRv.setLayoutManager(new LinearLayoutManager(this));
        filterBtn = findViewById(R.id.filterBtn);
        fab = findViewById(R.id.myLoc);
        bottomSheet=findViewById(R.id.sheet);
        bottomSheetBehavior=BottomSheetBehavior.from(bottomSheet);

        //starting location of halifax center
        currentLocation = new LatLng(44.648766, -63.575237);

        //get all the routes for the filter list from the routes.csv in raw folder
        getRoutes();

        adapter = new BusAdapter(MainActivity.this, routes);
        busRv.setAdapter(adapter);

        //Check if the permissions have been given or not
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {



            //ask for permissions
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);

            if(isFirstRunComplete) {
                //if the user has selected don't ask again while denying permissions show a dialog box
                if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Permission Denied")
                            .setMessage("Please enable the permission in \n Settings>Apps and Notifications>Transit>Permission \n and tick 'location' permission")
                            .setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(Settings.ACTION_SETTINGS));
                                }

                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    finish();
                                }
                            })
                            .show();
                }
            }

        } else {
            //if permissions given then start listening for location updates
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 200, listener);

        }
        try {
            //pb file url for halifax data
            url = new URL("http://gtfs.halifax.ca/realtime/Vehicle/VehiclePositions.pb");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        //setting up maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MainActivity.this);

        mHandlerTask.run();

        fab.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                if (myMap != null) {
                    //get last location using fusedlocationprovider
                    locationProviderClient.getLastLocation()
                            .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                                @SuppressLint("MissingPermission")
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {
                                        // Logic to handle location object
                                        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18.0f));
                                        myMap.setMyLocationEnabled(true);
                                        myMap.getUiSettings().setMyLocationButtonEnabled(false);

                                    } else {

                                        //If there is no last location then get make a location request
                                        locationRequest = LocationRequest.create();
                                        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                        locationRequest.setInterval(2 * 1000);
                                        locationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                                            @Override
                                            public void onLocationResult(LocationResult locationResult) {
                                                super.onLocationResult(locationResult);

                                                //get the location and move camera towards it
                                                Location location = locationResult.getLastLocation();
                                                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                                myMap.setMyLocationEnabled(true);
                                                myMap.getUiSettings().setMyLocationButtonEnabled(false);
                                                myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18.0f));
                                                locationProviderClient.removeLocationUpdates(this);
                                            }

                                            @Override
                                            public void onLocationAvailability(LocationAvailability locationAvailability) {
                                                super.onLocationAvailability(locationAvailability);
                                                if (!locationAvailability.isLocationAvailable()) {
                                                    //if the location is turned off
                                                    Toast.makeText(MainActivity.this, "Unable to fetch current location", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        }, null);
                                    }
                                }
                            });

                }
            }
        });

        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get all the selected routes
                wantedRoutes = adapter.giveMeRoutes();

                //if there are no selected routes then show all routes
                if (wantedRoutes.size() == 0) {
                    new doInBackGround().execute();
                    Toast.makeText(MainActivity.this, "Showing all buses", Toast.LENGTH_LONG).show();
                }else {
                    new doInBackGround().execute();
                }
                //hide the bottom sheet
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
    }

    //Run this method in different threads for every 15 secs
    final Runnable mHandlerTask = new Runnable() {
        @Override
        public void run() {
            new doInBackGround().execute();
            mHandler.postDelayed(mHandlerTask, 15000);
        }
    };


    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();

        //get the saved state of the map and location and recreate it.
        SharedPreferences pref = getSharedPreferences("MapInstance", MODE_PRIVATE);
        LatLng coords = new LatLng(Double.valueOf(pref.getString("latitude", "44.648766")), Double.valueOf(pref.getString("longitude", "-63.575237")));
        float zoom = pref.getFloat("zoom", 18.0f);
        isFirstRunComplete=pref.getBoolean("isFirstRunComplete",false);

        if (myMap != null) {
            if(isPermissionGranted){
                myMap.setMyLocationEnabled(true);
                myMap.getUiSettings().setMyLocationButtonEnabled(false);
            }

            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coords, zoom));

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences pref = getSharedPreferences("MapInstance", MODE_PRIVATE);
        isFirstRunComplete=pref.getBoolean("isFirstRunComplete",false);
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

    //save the state of the map and the camera position into shared preferences.
    public void saveState() {
        if (myMap != null) {
            CameraPosition cameraPosition = myMap.getCameraPosition();
            LatLng coords = new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);
            float zoom = cameraPosition.zoom;

            SharedPreferences pref = getSharedPreferences("MapInstance", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();

            editor.putString("latitude", String.valueOf(coords.latitude));
            editor.putString("longitude", String.valueOf(coords.longitude));
            editor.putBoolean("isFirstRunComplete",true);
            editor.putFloat("zoom", zoom);
            editor.apply();
        }

    }


    //called when map is all loaded
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        myMap = googleMap;

        //get saved data when map loads
        SharedPreferences pref = getSharedPreferences("MapInstance", MODE_PRIVATE);
        LatLng coords = new LatLng(Double.valueOf(pref.getString("latitude", "44.648766")), Double.valueOf(pref.getString("longitude", "-63.575237")));
        float zoom = pref.getFloat("zoom", 18.0f);



        if (myMap != null) {
            if(isPermissionGranted){
                //This code shows the blue dot on the map.
                myMap.setMyLocationEnabled(true);
                myMap.getUiSettings().setMyLocationButtonEnabled(false);
            }

            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coords, zoom));


        }
    }

    //This listener listens the any changes in user location and move the map camera according to it.
    LocationListener listener = new LocationListener() {
        @SuppressLint("MissingPermission")
        @Override
        public void onLocationChanged(Location location) {

            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

            if (myMap != null) {


                if(isPermissionGranted){
                    myMap.setMyLocationEnabled(true);
                    myMap.getUiSettings().setMyLocationButtonEnabled(false);
                }


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

    //This class runs in the background and fetches the real time data.
    public static class doInBackGround extends AsyncTask<Void, Void, List<GtfsRealtime.FeedEntity>>{


        //make the request.
        @Override
        protected List<GtfsRealtime.FeedEntity> doInBackground(Void... voids) {


            try {

                return GtfsRealtime.FeedMessage.parseFrom(url.openStream()).getEntityList();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        //this is called when the request is complete
        @SuppressLint("MissingPermission")
        @Override
        protected void onPostExecute(List<GtfsRealtime.FeedEntity> feedEntities) {
            super.onPostExecute(feedEntities);

            myMap.clear();
            if(isPermissionGranted){
                myMap.setMyLocationEnabled(true);
                myMap.getUiSettings().setMyLocationButtonEnabled(false);
            }

            try {

                    for (GtfsRealtime.FeedEntity entity : feedEntities) {
                        if (entity.hasVehicle()) {
                            //if the user has selected any routes then show only those buses.
                            if (wantedRoutes.size() > 0) {
                                if (wantedRoutes.contains(entity.getVehicle().getTrip().getRouteId())) {
                                    makeMarkers(entity);
                                }
                            } else {
                                //else show all the buses
                                makeMarkers(entity);
                            }
                        }
                    }

            }catch (Exception e){
                //if there is no internet connection
                Toast.makeText(context,"Please check your Internet Connection",Toast.LENGTH_LONG).show();
            }


        }
    }

    //This method makes markers on the map
    public static  void makeMarkers(GtfsRealtime.FeedEntity entity){
        if(entity.getVehicle().hasPosition()){

            //This code creates new specialised icon for each bus with their number
            IconGenerator icg = new IconGenerator(context);
            icg.setBackground(context.getResources().getDrawable(R.drawable.marker_bus));
            icg.setTextAppearance(R.style.icon_style); // black text
            Bitmap bm = icg.makeIcon(entity.getVehicle().getTrip().getRouteId());

            //add properties to the marker
            MarkerOptions options = new MarkerOptions();
            options.position(new LatLng(entity.getVehicle().getPosition().getLatitude(), entity.getVehicle().getPosition().getLongitude()));
            options.icon(BitmapDescriptorFactory.fromBitmap(bm));
            options.title(entity.getVehicle().getTrip().getRouteId());

            //extra data for each bus in a custom info window
            InfoWindowModel model=new InfoWindowModel();
            model.setStopTxt(entity.getVehicle().getCurrentStatus().getValueDescriptor().toString());
            model.setCongestionTxt(entity.getVehicle().getCongestionLevel().getValueDescriptor().toString());
            model.setIncomingTxt(entity.getVehicle().getTrip().getRouteId());
            MarkerInfoAdapter adapter=new MarkerInfoAdapter(context);
            myMap.setInfoWindowAdapter(adapter);

            //add marker to the map
            m= myMap.addMarker(options);
            m.setTag(model);

        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //If the permission is granted
                    isPermissionGranted=true;
                    manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 200, listener);
                    if(myMap!=null) {
                        myMap.setMyLocationEnabled(true);

                        myMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }// permission was granted, yay! Do the
                    // contacts-related task you need to do.
                }else {
                    //if permission is denied then re ask the permissions
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            1);

                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


    }

    //get all the routes from the static data which is stored as a csv in the raw folder.
    public void getRoutes(){

        InputStream inputStream = getResources().openRawResource(R.raw.routes);
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(inputStream, Charset.forName("UTF-8")));
        String line = "";
        try {
            while ((line = bufferedReader.readLine()) != null) {
                String[] data = line.split(",");

                //extract all the routes and add it to the list.
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
