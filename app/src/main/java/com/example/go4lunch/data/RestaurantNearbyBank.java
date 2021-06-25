package com.example.go4lunch.data;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.go4lunch.R;
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
import java.util.Objects;

public class RestaurantNearbyBank {
    public interface ListAsyncResponse{
        void processFinished(List<Restaurant> restaurantList);
    }

    public interface OnMarkerClicked{
        void onMarkerClickedGetRestaurant(Restaurant restaurant);
    }

    private GoogleMap mGoogleMap;
    private final Context mContext;
    private final RequestQueue mRequestQueue;
    private final OnMarkerClicked mMarkerClickedCallback;
    private List<Restaurant> mRestaurantList = new ArrayList<>();

    public RestaurantNearbyBank(Context context, GoogleMap googleMap) {
        mContext = context;
        mGoogleMap = googleMap;
        mMarkerClickedCallback = (OnMarkerClicked) context;
        mRequestQueue = RequestQueueSingleton.getInstance(context).getRequestQueue();
    }

    public RestaurantNearbyBank(Context context) {
        mContext = context;
        mMarkerClickedCallback = (OnMarkerClicked) context;
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

    public void getRestaurantNearbyList(String url, final ListAsyncResponse listResponseCallback){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d("URL", "getRestaurantNearbyList: " + url);
                    try {
                        if (!mRestaurantList.isEmpty())
                            mRestaurantList = new ArrayList<>();

                        JSONArray results = response.getJSONArray(Constants.RESULTS);
                        int restaurantIndex = 0;
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject resultObject = results.getJSONObject(i);
                            String name = resultObject.getString(Constants.NAME);

                            JSONObject geometry = resultObject.getJSONObject(Constants.GEOMETRY);
                            JSONObject location = geometry.getJSONObject(Constants.LOCATION);
                            double lat = location.getDouble(Constants.LATITUDE);
                            double lng = location.getDouble(Constants.LONGITUDE);
                            LatLng position = new LatLng(lat, lng);

                            JSONArray typeArray = resultObject.getJSONArray(Constants.TYPE);
                            List<String> typeList = new ArrayList<>();
                            for (int y = 0; y < typeArray.length(); y++){
                                String type = typeArray.getString(y);
                                typeList.add(type);
                            }

                            if (typeList.contains(Constants.RESTAURANT) && !typeList.contains(Constants.LODGING)){

                                String photoReference = "";
                                JSONArray photoArray = resultObject.getJSONArray(Constants.PHOTOS);
                                for (int z = 0; z < photoArray.length(); z++){
                                    JSONObject photoObject = photoArray.getJSONObject(z);
                                    photoReference = photoObject.getString(Constants.PHOTO_REFERENCE);
                                }

                                String photoUrl = Constants.PLACE_PHOTO_SEARCH_URL +
                                        "maxwidth=" + Constants.PHOTO_MAX_WIDTH +
                                        "&photoreference=" + photoReference +
                                        "&key=" + mContext.getString(R.string.google_maps_key);

                                float rating = resultObject.getInt(Constants.RATING);
                                int favorableOpinion;

                                if (rating >= 3)
                                    favorableOpinion = 3;
                                else
                                    favorableOpinion = (int) rating;

                                String address = getStreetAddressFromPositions(position);

                                Restaurant restaurant = new Restaurant();
                                restaurant.setName(name);
                                restaurant.setAddress(address);
                                restaurant.setPosition(position);
                                restaurant.setFavorableOpinion(favorableOpinion);

                                if (!photoReference.equals(""))
                                    restaurant.setImageUrl(photoUrl);

                                mRestaurantList.add(restaurant);

                                if (mGoogleMap != null)
                                    addMarkerOnPosition(mGoogleMap, position, name, restaurantIndex);

                                restaurantIndex++;
                            }

                        }

                        if (listResponseCallback != null)
                            listResponseCallback.processFinished(mRestaurantList);

                        if (mMarkerClickedCallback != null && mGoogleMap != null) {
                            mGoogleMap.setOnMarkerClickListener(marker -> {
                                int tag = -2;
                                if (marker.getTag() != null)
                                    tag = (Integer)marker.getTag();
                                if (tag != -1) //if the marker doesn't correspond to the device location
                                    mMarkerClickedCallback.onMarkerClickedGetRestaurant(mRestaurantList.get(tag));

                                return false;
                            });
                        }

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

    private void addMarkerOnPosition(GoogleMap googleMap, LatLng position, String title, int restaurantIndex){
        Objects.requireNonNull(googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))))
        .setTag(restaurantIndex);
    }
}
