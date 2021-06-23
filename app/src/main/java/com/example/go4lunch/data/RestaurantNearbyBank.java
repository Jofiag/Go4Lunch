package com.example.go4lunch.data;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.go4lunch.model.Restaurant;
import com.example.go4lunch.util.Constants;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RestaurantNearbyBank {
    public interface ListAsyncResponse{
        void processFinished(List<Restaurant> restaurantList);
    }

    private GoogleMap mGoogleMap;
    private final Context mContext;
    private final RequestQueue mRequestQueue;
    private List<Restaurant> restaurantList = new ArrayList<>();

    public RestaurantNearbyBank(Context context, GoogleMap googleMap) {
        mContext = context;
        mGoogleMap = googleMap;
        mRequestQueue = RequestQueueSingleton.getInstance(context).getRequestQueue();
    }

    public RestaurantNearbyBank(Context context) {
        mContext = context;
        mRequestQueue = RequestQueueSingleton.getInstance(context).getRequestQueue();
    }

    public static synchronized RestaurantNearbyBank getInstance(Context context, GoogleMap googleMap){
        RestaurantNearbyBank INSTANCE;

        if (googleMap == null)
            INSTANCE = new RestaurantNearbyBank(context);
        else
            INSTANCE = new RestaurantNearbyBank(context, googleMap);

        return INSTANCE;
    }

    public void getRestaurantNearbyList(String url, boolean addMarker, final ListAsyncResponse callback){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (!restaurantList.isEmpty())
                            restaurantList = new ArrayList<>();

                        JSONArray results = response.getJSONArray(Constants.RESULTS);
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject resultObject = results.getJSONObject(i);
                            String name = resultObject.getString(Constants.NAME);
//                            String reference = resultObject.getString(Constants.REFERENCE);
//                            String vicinity = resultObject.getString(Constants.VICINITY);


                            JSONObject geometry = resultObject.getJSONObject(Constants.GEOMETRY);
                            JSONObject location = geometry.getJSONObject(Constants.LOCATION);
                            double lat = location.getDouble(Constants.LATITUDE);
                            double lng = location.getDouble(Constants.LONGITUDE);
                            LatLng position = new LatLng(lat, lng);

                            if (addMarker)
                                addMarkerOnPosition(mGoogleMap, position, name);


                            String address = getStreetAddressFromPositions(position);
                            Log.d("JSON1", "\ngeometry : " + geometry + "\n" +
                                    "location : " + location + "\n" +
                                    "lat : " + lat + "\n" +
                                    "lng : " + lng + "\n" +
                                    "Street address : " + address);

                            Restaurant restaurant = new Restaurant();
                            restaurant.setName(name);
                            restaurant.setAddress(address);
                            restaurant.setPosition(position);

//                            addMarkerOnPosition(googleMap, restaurant.getPosition(), restaurant.getName(), BitmapDescriptorFactory.HUE_ORANGE);

                            restaurantList.add(restaurant);
                        }

                        if (callback != null)
                            callback.processFinished(restaurantList);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.d("VOLLEY", "onErrorResponse: " + error.getMessage()));

        mRequestQueue.add(jsonObjectRequest);

    }

    private String getStreetAddressFromPositions(LatLng position) {
        String address = "";
        Geocoder geocoder = new Geocoder(mContext);
        List<Address> addressList;

        try {
            addressList = geocoder.getFromLocation(position.latitude, position.longitude, 1);
            address = addressList.get(0).getAddressLine(0);
        } catch (IOException e) {
            Log.d("ADDRESS", "getStreetAddressFromPositions: " + e.getMessage());
        }

        return address;
    }

    private void addMarkerOnPosition(GoogleMap googleMap, LatLng position, String title){
        googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
    }
}
