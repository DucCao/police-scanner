package com.condorhero89.policescanner;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class MainActivity extends Activity {
    public interface OnLocationChangeListener {
        public void onLocationChanged(final Location location);
    }
    
    private static final float MAX_DISTANCE = 1000;
    
    public static final String LOCATION_OBJECT = "LocationObject";
    public static final String KEY_LAT = "lat";
    public static final String KEY_LNG = "lng";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_DESCRIPTION = "description";
    
    private GoogleMap map;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    
    private TextView txtScanPolice;
    
    private HashMap<String, Marker> mapMarkers = new HashMap<String, Marker>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Parse.initialize(this, "zIYDZTEWwL8vBrPiqnW0p8dArdjKh3tfjX5dMy7U", "olCWVjSxY1OxooRARGioGHR4R7JXGXQrzUbWfDDL"); 
        
        setContentView(R.layout.activity_main);
        
        txtScanPolice = (TextView) findViewById(R.id.txtScanPolice);
        
        getFragmentManager().findFragmentById(R.id.map);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        
        map.setOnCameraChangeListener(new OnCameraChangeListener() {
            
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
//                Log.i("OnCameraChangeListener", "onCameraChange: " + cameraPosition);
            }
        });
        
        map.setMyLocationEnabled(true);
        
        queryAllPoliceData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        Log.e("LocationManager", "onDestroy: removeUpdates");
        if (mLocationListener != null) {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }
    
    public void reportPolice(View view) {
        retrieveCurrentLocationAndReport();
    }
    
    public void scanPolice(View view) {
        requestLocation("Scanning...", new OnLocationChangeListener() {
            
            @Override
            public void onLocationChanged(final Location location) {
                Log.e("LocationManager", "removeUpdates");
                mLocationManager.removeUpdates(mLocationListener);
                
                queryAllPoliceData("Scanning...", new FindCallback() {
                    public void done(List<ParseObject> listPoliceData, ParseException e) {
                        if (e == null) {
                            Log.e("Parse", "Retrieved " + listPoliceData.size() + " object(s)");
                            
                            // clear all markers first
                            for (Marker marker : mapMarkers.values()) {
                                marker.remove();
                            }
                            
                            // update markers state
                            boolean isSafe = true;
                            for (ParseObject parseObject : listPoliceData) {
                                LatLng latLng = new LatLng(parseObject.getDouble(KEY_LAT), parseObject.getDouble(KEY_LNG));
                                float distance = Util.distFrom(location.getLatitude(), location.getLongitude(), 
                                        latLng.latitude, latLng.longitude);
                                Log.e("TEST", "distance = " + distance);
                                
                                String address = parseObject.getString(KEY_ADDRESS);
                                if (distance < MAX_DISTANCE) {
//                                    Marker marker = mapMarkers.get(address);
//                                    marker.remove();
                                    
                                    addDangerMarker(latLng, address);
                                    
                                    isSafe = false;
                                } else {
                                    addMarker(latLng, address);
                                }
                            }
                            
                            if (isSafe) {
                                txtScanPolice.setTextColor(Color.BLUE);
                                txtScanPolice.setText("SAFE");
                            } else {
                                txtScanPolice.setTextColor(Color.RED);
                                txtScanPolice.setText("DANGER");
                            }
                            
                        } else {
                            Log.e("Parse", "Error: " + e.getMessage());
                        }
                    }
                });
            }
        });
    }

    private void retrieveCurrentLocationAndReport() {
        requestLocation("F***ing this policeman...", new OnLocationChangeListener() {
            
            @Override
            public void onLocationChanged(Location location) {
                Log.e("LocationManager", "removeUpdates");
                mLocationManager.removeUpdates(mLocationListener);
                
                Log.i("LocationListener", "onLocationChanged: " + location);
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                
                PoliceData mPoliceData = new PoliceData(getApplicationContext(), location, "test");
                
                addMarker(currentLatLng, mPoliceData.getAddress());
                
                reportCurrentLocation(mPoliceData);
            }
        });
    }
    
    private void requestLocation(String message, final OnLocationChangeListener listener) {
        final ProgressDialog progressDialog = ProgressDialog.show(this, "", message);
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
                listener.onLocationChanged(location);
                progressDialog.dismiss();
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
    
    private void addMarker(LatLng currentLatLng, String address) {
        MarkerOptions markerOptions = new MarkerOptions()
            .position(currentLatLng)
            .title(address)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher));
        mapMarkers.put(address, map.addMarker(markerOptions));
    }
    
    private void addDangerMarker(LatLng currentLatLng, String address) {
        MarkerOptions markerOptions = new MarkerOptions()
            .position(currentLatLng)
            .title(address)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.danger));
        mapMarkers.put(address, map.addMarker(markerOptions));
    }
    
    private void queryAllPoliceData(String message, final FindCallback findCallback) {
        final ProgressDialog progressDialog = ProgressDialog.show(MainActivity.this, "", message);
        progressDialog.show();
        
        ParseQuery query = new ParseQuery(LOCATION_OBJECT);
        query.findInBackground(new FindCallback() {
            public void done(List<ParseObject> listPoliceData, ParseException e) {
                findCallback.done(listPoliceData, e);
                progressDialog.dismiss();
            }
        });
    }
    
    private void queryAllPoliceData() {
        queryAllPoliceData("Loading...", new FindCallback() {
            public void done(List<ParseObject> listPoliceData, ParseException e) {
                if (e == null) {
                    Log.d("Parse", "Retrieved " + listPoliceData.size() + " object(s)");
                    for (ParseObject parseObject : listPoliceData) {
                        LatLng latLng = new LatLng(parseObject.getDouble(KEY_LAT), parseObject.getDouble(KEY_LNG));
                        String address = parseObject.getString(KEY_ADDRESS);
                        addMarker(latLng, address);
                    }
                } else {
                    Log.e("Parse", "Error: " + e.getMessage());
                }
            }
        });
    }
}
