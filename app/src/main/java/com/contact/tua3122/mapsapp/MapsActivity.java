package com.contact.tua3122.mapsapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Location myLocation;
    private LocationManager locationManager;
    private List<Address> addressList;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private boolean gotMyLocationOneTime;
    private double latitude, longitude;
    private boolean notTrackingMyLocation = true;
    private EditText locationSearch;

    private static final long MIN_TIME_BW_UPDATES = 1000 * 5;
    private static final float MIN_DISTANCE_cHANGE_FOR_UPDATES = 0.0f;
    private static final int MY_LOC_ZOOM_FACTOR = 17;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

        //Add a marker at your place of birth and move the camera to it.
        //When the marer is tapped, display "Born here"
//        LatLng maryland = new LatLng(39, -77);
//        mMap.addMarker(new MarkerOptions().position(maryland).title("Born Here"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(maryland));


//        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
//            Log.d("MapsApp", "Failed  FINE permission check.");
//            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},2);
//
//        }
//
//        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
//            Log.d("MapsApp", "Failed COARSE permission check.");
//            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},2);
//
//        }
//
//        if((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)||
//            (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)){
//            mMap.setMyLocationEnabled(true);
//        }
        locationSearch = (EditText) findViewById(R.id.editText_address);

        gotMyLocationOneTime = false;
        getLocation();

    }


    //Add changeView to switch between map and satellite
    public void changeView(View view){
        if(mMap.getMapType()==GoogleMap.MAP_TYPE_SATELLITE){
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        else{
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }

    }

    public void onSearch(View view){
        String location = locationSearch.getText().toString();

        List<Address> addressList= null;
        List<Address> addressListZip=null;
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);

        Log.d("MapsApp", "onSearch: location = " + location);
        Log.d("MapsApp", "onSearch: provider = " + provider);
        LatLng userLocation = null;

        try{
            if(locationManager != null){
                Log.d("MapsApp", "onSearch: locationManager is not null.");

                if((myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER))!= null){
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MapsApp", "onSearch: using NETWORK_PROVIDER userLocation is: "
                            + myLocation.getLatitude() + ", " + myLocation.getLongitude());
                    Toast.makeText(this, "UserLoc" + myLocation.getLatitude() + ", " + myLocation.getLongitude(), Toast.LENGTH_SHORT);

                } else if((myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER))!= null){
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MapsApp", "onSearch: using GPS_PROVIDER userLocation is: "
                            + myLocation.getLatitude() + ", " + myLocation.getLongitude());
                    Toast.makeText(this, "UserLoc" + myLocation.getLatitude() + ", " + myLocation.getLongitude(), Toast.LENGTH_SHORT);
                } else{
                    Log.d("MapsApp", "onSearch: myLocation is null from getLastKnownLocation.");

                }
            }
        }
        catch (SecurityException | IllegalArgumentException e){
            Log.d("MapsApp", "onSearch: Exception getLastKnownLocation.");
            Toast.makeText(this, "onSearch: Exception getLastKnownLocation.", Toast.LENGTH_SHORT);

        }

        if(!location.matches("")){
            Log.d("MapsApp", "onSearch: location field is populated");
            Geocoder geocoder = new Geocoder(this, Locale.US);
            try{
                addressList = geocoder.getFromLocationName(location, 100, userLocation.latitude - (5.0/60), userLocation.longitude - (5.0/60), userLocation.latitude + (5.0/60), userLocation.longitude + (5.0/60));
                Log.d("MapsApp", "onSearch: addressList is created");

            }
            catch (IOException e){
                e.printStackTrace();
            }
            if(!addressList.isEmpty()){
                Log.d("MapsApp", "onSearch: AddressList size is: " + addressList.size());
                for(int i = 0; i < addressList.size(); i++){
                    Address address = addressList.get(i);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLng).title(i+": " + address.getSubThoroughfare() + address.getSubThoroughfare()));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                }
            }
        }

    }

    public void getLocation(){
        try{
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            //Get GPS status, isProviderEnabled returns true if user has enabled gps
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(isGPSEnabled){
                Log.d("MapsApp", "getLocation: GPS is enabled");
            }
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if(isNetworkEnabled){
                Log.d("MapsApp", "getLocation: Network is enabled");
            }

            if(!isGPSEnabled&&!isNetworkEnabled){
                Log.d("MapsApp", "getLocation: No provider enabled");
            }
            else{
              if(isNetworkEnabled){
                  //Request location updates
                  if((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
                  && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                      return;
                  }
                  locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_cHANGE_FOR_UPDATES, locationListenerNetwork);
              }if(isGPSEnabled){
                    //Request location updates
                    if((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_cHANGE_FOR_UPDATES, locationListenerGPS);
                }
            }
        }catch(Exception e){
            Log.d("MapsApp", "getLocation: Exception in getLocation");
            e.printStackTrace();
        }
    }

    //LocationListener to setup callbacks for requestLocationUpdates
    LocationListener locationListenerNetwork = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            dropAMarker(LocationManager.NETWORK_PROVIDER);
            //Check if doing one time, if so, remove updates to both gps and network
            if(gotMyLocationOneTime==false){
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerGPS);
                gotMyLocationOneTime = true;
            }
            else{
                if(isNetworkEnabled){
                    //Request location updates
                    if((ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
                            && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_cHANGE_FOR_UPDATES, locationListenerNetwork);
                }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MapsApp", "locationListenerNetwork: Status change");
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            dropAMarker(LocationManager.GPS_PROVIDER);
            //Check if doing one time, if so, remove updates to both gps and network
            if(gotMyLocationOneTime==false){
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerNetwork);
                gotMyLocationOneTime = true;
            }
            else{
//                if(isGPSEnabled){
//                    //Request location updates
//                    if((ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
//                            && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
//                        return;
//                    }
//                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_cHANGE_FOR_UPDATES, locationListenerGPS);
//                }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MapsApp", "locationListenerGPS: Status change");
            switch (status) {
            case LocationProvider.AVAILABLE:
                break;
            case LocationProvider.OUT_OF_SERVICE:
                    //enable network updates
                    isNetworkEnabled=true;
            break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                //enable both network and GPS
                isNetworkEnabled=true;
                isGPSEnabled=true;
                break;
            default:
                //enable both network and gps
                isNetworkEnabled=true;
                isGPSEnabled=true;
            }

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public void dropAMarker(String provider){
        if(locationManager!=null){
//            if(checkSelfPermission fails){
//                return;
//            }
            if((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ||(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)){
                Log.d("MapsApp", "dropAMarker: Failed checkSelfPermission.");
                return;
            }

            myLocation = locationManager.getLastKnownLocation(provider);
            LatLng userLocation = null;
            if(myLocation==null){
                Log.d("MapsApp", "dropAMarker: myLocation == null");

            }
            else{
                userLocation = new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);
                if(provider==locationManager.GPS_PROVIDER){
                    //add circle for the marker with 2 outer rings
                    mMap.addCircle(new CircleOptions()
                            .center(userLocation)
                            .radius(1)
                            .strokeColor(Color.RED)
                            .strokeWidth(2)
                            .fillColor(Color.RED));
                    mMap.addCircle(new CircleOptions()
                            .center(userLocation)
                            .radius(2)
                            .strokeColor(Color.RED)
                            .strokeWidth(2)
                            .fillColor(Color.TRANSPARENT));
                }
                else{
                    mMap.addCircle(new CircleOptions()
                            .center(userLocation)
                            .radius(1)
                            .strokeColor(Color.BLUE)
                            .strokeWidth(2)
                            .fillColor(Color.BLUE));
                    mMap.addCircle(new CircleOptions()
                            .center(userLocation)
                            .radius(2)
                            .strokeColor(Color.BLUE)
                            .strokeWidth(2)
                            .fillColor(Color.TRANSPARENT));
                }
                mMap.animateCamera(update);
            }

        }

    }

    public void trackMyLocation(View view){
        //tick off the location tracker using getLocation to start the LocationListener
        if(notTrackingMyLocation){
            getLocation();
            notTrackingMyLocation = false;
        }
        else{
            locationManager.removeUpdates(locationListenerGPS);
            locationManager.removeUpdates(locationListenerNetwork);
            notTrackingMyLocation = true;
        }
    }

    public void clearMarkers(View view){
        mMap.clear();
    }
}
