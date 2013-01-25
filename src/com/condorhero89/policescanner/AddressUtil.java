package com.condorhero89.policescanner;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

public class AddressUtil {
    private static Geocoder sGeocoder;
    
    public static String getAddressFrom(Context context, Location location) {
        if (sGeocoder == null) {
            sGeocoder = new Geocoder(context);
        }
        
        try {
            List<Address> listAddress = sGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (listAddress != null && listAddress.size() > 0) {
                Address address = listAddress.get(0);
                int numOfAddressLine = address.getMaxAddressLineIndex();
                String result = "";
                for (int i = 0; i < numOfAddressLine; ++i) {
                    result += address.getAddressLine(i);
                }
                
                return result;
            }
        } catch (IOException e) {
        }
        
        return null;
    }
}
