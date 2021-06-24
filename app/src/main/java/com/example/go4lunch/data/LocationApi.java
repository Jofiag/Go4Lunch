package com.example.go4lunch.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class LocationApi {
    private Location location;
    private final Context context;
    private final Geocoder geocoder;
    @SuppressLint("StaticFieldLeak")
    private static LocationApi INSTANCE;


    public LocationApi(Context context) {
        this.context = context;
        geocoder = new Geocoder(context);
    }

    public static LocationApi getInstance(Context context) {
        if (INSTANCE == null)
            INSTANCE = new LocationApi(context);

        return INSTANCE;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public LatLng getPositionFromLocation(){
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public String getStreetAddressFromPositions() {
        LatLng position = getPositionFromLocation();

        String address = "";

        List<Address> addressList;

        try {
            addressList = geocoder.getFromLocation(position.latitude, position.longitude, 1);
            address = addressList.get(0).getAddressLine(0);
        } catch (IOException e) {
            Log.d("ADDRESS", "getStreetAddressFromPositions: " + e.getMessage());
        }

        return address;
    }
}
