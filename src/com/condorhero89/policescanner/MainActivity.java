package com.condorhero89.policescanner;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.parse.Parse;
import com.parse.ParseObject;

public class MainActivity extends Activity {
    static final LatLng HAMBURG = new LatLng(53.558, 9.927);
    static final LatLng KIEL = new LatLng(53.551, 9.993);
    private GoogleMap map;
    private LocationManager mLocationManager;
    private Location mCurrentLocation;
    private LocationListener mLocationListener;

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
        
        retrieveCurrentLocation();
        
        ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "bar");
        testObject.saveInBackground();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        Log.i("LocationManager", "removeUpdates");
        mLocationManager.removeUpdates(mLocationListener);
    }

    private void retrieveCurrentLocation() {
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
                Log.i("LocationListener", "onLocationChanged: " + location);
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                
//                map.addMarker(new MarkerOptions()
//                      .position(currentLatLng)
//                      .title("current location")
//                      .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher)));
                
                mCurrentLocation = location;
            }
        };
        
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
    }
}
