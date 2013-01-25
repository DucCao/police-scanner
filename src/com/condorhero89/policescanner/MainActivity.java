package com.condorhero89.policescanner;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.parse.Parse;
import com.parse.ParseObject;

public class MainActivity extends Activity {
    public static final String LOCATION_OBJECT = "LocationObject";
    public static final String KEY_LAT = "lat";
    public static final String KEY_LNG = "lng";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_DESCRIPTION = "description";
    
    private GoogleMap map;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private PoliceData mPoliceData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Parse.initialize(this, "zIYDZTEWwL8vBrPiqnW0p8dArdjKh3tfjX5dMy7U", "olCWVjSxY1OxooRARGioGHR4R7JXGXQrzUbWfDDL"); 
        
        setContentView(R.layout.activity_main);
        
        getFragmentManager().findFragmentById(R.id.map);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        
        map.setOnCameraChangeListener(new OnCameraChangeListener() {
            
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                Log.i("OnCameraChangeListener", "onCameraChange: " + cameraPosition);
            }
        });
        
        map.setMyLocationEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        Log.e("LocationManager", "onDestroy: removeUpdates");
        mLocationManager.removeUpdates(mLocationListener);
    }
    
    public void reportPolice(View view) {
        retrieveCurrentLocationAndReport();
    }

    private void retrieveCurrentLocationAndReport() {
        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "Getting current location...");
        progressDialog.show();
        
        mLocationListener = new LocationListener() {
            
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
            
            @Override
            public void onProviderEnabled(String provider) {
            }
            
            @Override
            public void onProviderDisabled(String provider) {
            }
            
            @Override
            public void onLocationChanged(Location location) {
                Log.e("LocationManager", "removeUpdates");
                mLocationManager.removeUpdates(mLocationListener);
                
                Log.i("LocationListener", "onLocationChanged: " + location);
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                
//                map.addMarker(new MarkerOptions()
//                      .position(currentLatLng)
//                      .title("current location")
//                      .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher)));
                
                mPoliceData = new PoliceData(getApplicationContext(), location, "test");
                
                progressDialog.dismiss();
                
                reportCurrentLocation(mPoliceData);
            }
        };
        
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, mLocationListener);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 0, mLocationListener);
    }
    
    private void reportCurrentLocation(PoliceData policeData) {
        Log.i("MainActivity", "reportCurrentLocation: " + policeData);
        
        ParseObject locationObject = new ParseObject(LOCATION_OBJECT);
        locationObject.put(KEY_LAT, policeData.getLocation().getLatitude());
        locationObject.put(KEY_LNG, policeData.getLocation().getLongitude());
        locationObject.put(KEY_ADDRESS, policeData.getAddress());
        locationObject.put(KEY_DESCRIPTION, policeData.getDescription());
        locationObject.saveInBackground();
    }
}
