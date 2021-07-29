package com.example.go4lunch.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.go4lunch.R;
import com.example.go4lunch.model.MyOpeningHours;
import com.example.go4lunch.model.Restaurant;
import com.example.go4lunch.util.Constants;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.LocalTime;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.Period;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class RestaurantNearbyBank2 {

    public interface OnRestaurantListCallback{
        void onListReady(ArrayList<Restaurant> restaurantList);
    }

    private final Context mContext;
    private final RequestQueue mRequestQueue;
    @SuppressLint("StaticFieldLeak")
    public static RestaurantNearbyBank2 INSTANCE;
    private ArrayList<Restaurant> mRestaurantList = new ArrayList<>();

    public RestaurantNearbyBank2(Context context) {
        this.mContext = context;
        mRequestQueue = RequestQueueSingleton.getInstance(context).getRequestQueue();
    }

    public synchronized static RestaurantNearbyBank2 getInstance(Context context){
        if (INSTANCE == null)
            INSTANCE = new RestaurantNearbyBank2(context);

        return INSTANCE;
    }



    public void getRestaurantList(String url, final OnRestaurantListCallback callback){
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            JSONArray results = response.getJSONArray(Constants.RESULTS);
                            int length = results.length();
                            for (int i = 0; i < length; i++) {
                                Restaurant restaurant = new Restaurant();
                                JSONObject resultObject = results.getJSONObject(i);
                                setAttributes(restaurant, resultObject, getPlaceTypeList(resultObject), callback);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Log.d("VOLLEY", "onErrorResponse: " + error.getMessage()));

            mRequestQueue.add(jsonObjectRequest);
    }
    private void setAttributes(Restaurant restaurant, JSONObject jsonObject, List<String> typeList, OnRestaurantListCallback callback) throws JSONException {
        if (typeList.contains(Constants.RESTAURANT) && !typeList.contains(Constants.LODGING)){
            getAndSetRestaurantName(restaurant, jsonObject);
            getAndSetRestaurantPositionAndAddress(restaurant, jsonObject);


            getAndSetRestaurantImageUrl(restaurant, jsonObject);
            getAndSetRestaurantRating(restaurant, jsonObject);
            getAndSetRestaurantPlaceID(restaurant, jsonObject);

            setDetails(restaurant, restaurant.getPlaceId(), callback);
        }
    }
    private void setDetails(Restaurant restaurant, String placeId, OnRestaurantListCallback callback){

        if(!Places.isInitialized())
            Places.initialize(Objects.requireNonNull(mContext), mContext.getString(R.string.google_maps_key));

        PlacesClient placesClient = Places.createClient(Objects.requireNonNull(mContext));
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.OPENING_HOURS, Place.Field.WEBSITE_URI, Place.Field.PHONE_NUMBER, Place.Field.UTC_OFFSET);
        FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.newInstance(placeId, placeFields);

        if (!mRestaurantList.isEmpty()){
            mRestaurantList = new ArrayList<>();
        }


        placesClient.fetchPlace(fetchPlaceRequest)
                .addOnSuccessListener(response -> {
                    Place place = response.getPlace();
                    Uri website = place.getWebsiteUri();
                    String phoneNumber = place.getPhoneNumber();
                    OpeningHours openingHours = place.getOpeningHours();

                    setRestaurantPhoneWebsiteAndMyOpeningHours(restaurant, place, phoneNumber, website, openingHours);

                    if (!mRestaurantList.contains(restaurant))
                        mRestaurantList.add(restaurant);

                    if (callback != null)
                        callback.onListReady(mRestaurantList);
                });

    }

    private void getAndSetRestaurantName(Restaurant restaurant, JSONObject jsonObject) throws JSONException {
        String name = jsonObject.getString(Constants.NAME);
        restaurant.setName(name);
    }
    private void getAndSetRestaurantImageUrl(Restaurant restaurant, JSONObject jsonObject) throws JSONException {
        String imageReference = "";
        JSONArray photoArray = jsonObject.getJSONArray(Constants.PHOTOS);
        for (int z = 0; z < photoArray.length(); z++){
            JSONObject photoObject = photoArray.getJSONObject(z);
            imageReference = photoObject.getString(Constants.PHOTO_REFERENCE);
        }

        String imageUrl = Constants.PLACE_PHOTO_SEARCH_URL +
                "maxwidth=" + Constants.PHOTO_MAX_WIDTH +
                "&photoreference=" + imageReference +
                "&key=" + mContext.getString(R.string.google_maps_key);

        if (!imageReference.equals(""))
            restaurant.setImageUrl(imageUrl);
    }
    private void getAndSetRestaurantRating(Restaurant restaurant, JSONObject jsonObject) throws JSONException {
        float rating = jsonObject.getInt(Constants.RATING);
        int favorableOpinion = 0;

        if (rating >= 4)
            favorableOpinion = 3;
        else if(rating == 3)
            favorableOpinion = 2;
        else if (rating < 3)
            favorableOpinion = (int) rating;

        restaurant.setFavorableOpinion(favorableOpinion);
    }
    private void getAndSetRestaurantPositionAndAddress(Restaurant restaurant, JSONObject jsonObject) throws JSONException {
        JSONObject geometry = jsonObject.getJSONObject(Constants.GEOMETRY);
        JSONObject location = geometry.getJSONObject(Constants.LOCATION);

        double lat = location.getDouble(Constants.LATITUDE);
        double lng = location.getDouble(Constants.LONGITUDE);

        LatLng position = new LatLng(lat, lng);
        restaurant.setPosition(position);

        String address = getStreetAddressFromPositions(position);
        restaurant.setAddress(address);
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
    private void getAndSetRestaurantPlaceID(Restaurant restaurant, JSONObject jsonObject) throws JSONException {
        String placeId = jsonObject.getString(Constants.PLACE_ID);
        restaurant.setPlaceId(placeId);
    }
    private List<String> getPlaceTypeList(JSONObject jsonObject) throws JSONException {
        JSONArray typeArray = jsonObject.getJSONArray(Constants.TYPE);
        List<String> typeList = new ArrayList<>();
        for (int y = 0; y < typeArray.length(); y++){
            String type = typeArray.getString(y);
            typeList.add(type);
        }

        return  typeList;
    }

    private MyOpeningHours castOpeningHoursToMyOpeningHours(OpeningHours openingHours){
        MyOpeningHours myOpeningHours = new MyOpeningHours();

        if (openingHours != null){
            int size = openingHours.getPeriods().size();
            boolean isOpen = isOpenToday(openingHours);
            String currentDayOfWeek = LocalDate.now().getDayOfWeek().toString();

            for (int i = 0; i < size; i++) {
                if (i+1 < size ) {
                    Period period = openingHours.getPeriods().get(i);
                    Period nextPeriod = openingHours.getPeriods().get(i+1);
                    String openDay = Objects.requireNonNull(period.getOpen()).getDay().toString();
                    String nextOpenDay = Objects.requireNonNull(nextPeriod.getOpen()).getDay().toString();

                    LocalTime firstOpeningTime = period.getOpen().getTime();
                    LocalTime lastOpeningTime = nextPeriod.getOpen().getTime();
                    LocalTime firstClosingTime = Objects.requireNonNull(period.getClose()).getTime();
                    LocalTime lastClosingTime = Objects.requireNonNull(nextPeriod.getClose()).getTime();

                    if (isOpen) {
                        myOpeningHours.setOpenToday(true);
                        if (openDay.equals(currentDayOfWeek) && nextOpenDay.equals(currentDayOfWeek)){
                            myOpeningHours.setFirstOpeningTime(firstOpeningTime);
                            myOpeningHours.setFirstClosingTime(firstClosingTime);
                            myOpeningHours.setLastClosingTime(lastOpeningTime);
                            myOpeningHours.setLastClosingTime(lastClosingTime);
                        }
                        else if (openDay.equals(currentDayOfWeek)){
                            myOpeningHours.setFirstOpeningTime(firstOpeningTime);
                            myOpeningHours.setFirstClosingTime(firstClosingTime);
                            myOpeningHours.setLastClosingTime(firstOpeningTime);
                            myOpeningHours.setLastClosingTime(firstClosingTime);
                        }
                    }
                    else
                        myOpeningHours.setOpenToday(false);
                }
            }
        }

        return myOpeningHours;
    }
    private boolean isOpenToday(OpeningHours placeOpeningHour){
        boolean isOpen = false;
        String currentDayOfWeek = LocalDate.now().getDayOfWeek().toString();

        int size = placeOpeningHour.getPeriods().size();
        for (int i = 0; i < size; i++) {
            Period period = placeOpeningHour.getPeriods().get(i);
            String openDay = Objects.requireNonNull(period.getOpen()).getDay().toString();

            if (openDay.equals(currentDayOfWeek))
                isOpen = true;

        }
        /*boolean isOpen = false;
        if(place.isOpen() != null)
            isOpen = place.isOpen();*/

        return  isOpen;
    }
    private void getAndSetRestaurantMyOpeningHours(Restaurant restaurant, OpeningHours openingHours){
        restaurant.setOpeningHours(castOpeningHoursToMyOpeningHours(openingHours));
    }
    private void setRestaurantPhoneWebsiteAndMyOpeningHours(Restaurant restaurant, Place place, String phoneNumber, Uri website, OpeningHours openingHours){
        getAndSetRestaurantMyOpeningHours(restaurant, openingHours);

        restaurant.setPhoneNumber(phoneNumber);
        restaurant.setWebsiteUrl(website);
        if (place.getLatLng() != null)
            restaurant.setDistanceFromDeviceLocation(getHowFarFrom(place.getLatLng()));
    }
    private int getHowFarFrom(LatLng destination){
        Location deviceLocation = LocationApi.getInstance(mContext).getLocation();
        Location destinationLocation = new Location("");
        destinationLocation.setLatitude(destination.latitude);
        destinationLocation.setLongitude(destination.longitude);


        return (int) deviceLocation.distanceTo(destinationLocation);
    }

}
