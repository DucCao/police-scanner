package com.condorhero89.policescanner;

import android.content.Context;
import android.location.Location;

public class PoliceData {
    private Location location;
    private String address;
    private String description;
    
    public PoliceData(Context context, Location location, String description) {
        this.location = location;
        this.address = AddressUtil.getAddressFrom(context, location);
        this.description = description;
    }
    
    public Location getLocation() {
        return location;
    }
    public String getAddress() {
        return address;
    }
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "PoliceData [location=" + location + ", address=" + address
                + ", description=" + description + "]";
    }
}
