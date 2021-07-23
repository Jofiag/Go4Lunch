package com.example.go4lunch.data;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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

@SuppressLint("StaticFieldLeak")
public class RestaurantNearbyBank {
    public interface ListAsyncResponse{
        void processFinished(List<Restaurant> restaurantList);
    }

    public interface OnMarkerClicked{
        void onMarkerClickedGetRestaurant(Restaurant restaurant);
    }

    private static GoogleMap mGoogleMap;
    private final Activity mActivity;
    private final RequestQueue mRequestQueue;
    private final OnMarkerClicked mMarkerClickedCallback;
    private final List<Restaurant> mRestaurantList = new ArrayList<>();
    public static RestaurantNearbyBank INSTANCE;

    public RestaurantNearbyBank(Activity activity, GoogleMap googleMap) {
        mActivity = activity;
        mGoogleMap = googleMap;
        mMarkerClickedCallback = (OnMarkerClicked) activity;
        mRequestQueue = RequestQueueSingleton.getInstance(activity).getRequestQueue();
    }

    public RestaurantNearbyBank(Activity activity) {
        mActivity = activity;
        mMarkerClickedCallback = (OnMarkerClicked) activity;
        mRequestQueue = RequestQueueSingleton.getInstance(activity).getRequestQueue();
    }

    public static synchronized RestaurantNearbyBank getInstance(Activity activity, GoogleMap googleMap){
        if (INSTANCE == null)
            INSTANCE = new RestaurantNearbyBank(activity, googleMap);
        else{
            if (googleMap == null)
                INSTANCE = new RestaurantNearbyBank(activity);
            else
                INSTANCE = new RestaurantNearbyBank(activity, googleMap);
        }

        return INSTANCE;
    }


    public void getRestaurantNearbyList(String url, final ListAsyncResponse listResponseCallback){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray(Constants.RESULTS);
                        int length = results.length();
                        for (int i = 0; i < length; i++) {
                            Restaurant restaurant = new Restaurant();
                            JSONObject resultObject = results.getJSONObject(i);
                            getAndSetRestaurantAttributes(restaurant, resultObject, getPlaceTypeList(resultObject), listResponseCallback);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.d("VOLLEY", "onErrorResponse: " + error.getMessage()));

        mRequestQueue.add(jsonObjectRequest);
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
                    "&key=" + mActivity.getString(R.string.google_maps_key);

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
        Geocoder geocoder = new Geocoder(mActivity);
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
        private void getAndSetRestaurantAttributes(Restaurant restaurant, JSONObject jsonObject, List<String> typeList, ListAsyncResponse listResponseCallback) throws JSONException {
            if (typeList.contains(Constants.RESTAURANT) && !typeList.contains(Constants.LODGING)){
                getAndSetRestaurantName(restaurant, jsonObject);
                getAndSetRestaurantPositionAndAddress(restaurant, jsonObject);

                if (mGoogleMap != null)
                    addMarkerOnPosition(mGoogleMap, restaurant.getPosition(), restaurant.getName(), restaurant.getAddress());

                getAndSetRestaurantImageUrl(restaurant, jsonObject);
                getAndSetRestaurantRating(restaurant, jsonObject);
                getAndSetRestaurantPlaceID(restaurant, jsonObject);

                setMoreRestaurantDetails(restaurant, restaurant.getPlaceId(), listResponseCallback);
            }
        }
        private void addMarkerOnPosition(GoogleMap googleMap, LatLng position, String title, String restaurantAddress){
        Objects.requireNonNull(googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))))
                .setTag(restaurantAddress);
    }


    private void setMoreRestaurantDetails(Restaurant restaurant, String placeId, ListAsyncResponse listResponseCallback){
            /*String detailsUrl = Constants.PLACE_DETAILS_SEARCH_URL +
                    "place_id=" + placeId +
                    "&key=" + mContext.getString(R.string.google_maps_key);*/

    //        Log.d("DETAILS", "getOpeningHours: DETAILS = " + detailsUrl);

            if(!Places.isInitialized())
                Places.initialize(Objects.requireNonNull(mActivity), mActivity.getString(R.string.google_maps_key));

            PlacesClient placesClient = Places.createClient(Objects.requireNonNull(mActivity));
            List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.OPENING_HOURS, Place.Field.WEBSITE_URI, Place.Field.PHONE_NUMBER, Place.Field.UTC_OFFSET);
            FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.newInstance(placeId, placeFields);


            placesClient.fetchPlace(fetchPlaceRequest)
                    .addOnSuccessListener(response -> {
                        Place place = response.getPlace();
                        Uri website = place.getWebsiteUri();
                        String phoneNumber = place.getPhoneNumber();
                        OpeningHours openingHours = place.getOpeningHours();

                        setRestaurantPhoneWebsiteAndMyOpeningHours(restaurant, place, phoneNumber, website, openingHours);
                        mRestaurantList.add(restaurant);
                        setTheOnMarkerClickListener();
                        sendRestaurantListToResponseCallback(listResponseCallback);

                    });

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
        Location deviceLocation = LocationApi.getInstance(mActivity).getLocation();
        Location destinationLocation = new Location("");
        destinationLocation.setLatitude(destination.latitude);
        destinationLocation.setLongitude(destination.longitude);


        return (int) deviceLocation.distanceTo(destinationLocation);
    }
        private void setTheOnMarkerClickListener(){
                if (mMarkerClickedCallback != null && mGoogleMap != null) {
                    mGoogleMap.setOnMarkerClickListener(marker -> {
                        if (!Objects.equals(marker.getTag(), Constants.DEVICE_POSITION)){ //if the marker doesn't correspond to the device location
                            for (Restaurant restaurant1 : mRestaurantList) {
                                if (restaurant1.getAddress().equals(marker.getTag())) {
                                    RestaurantSelectedApi.getInstance().setRestaurantSelected(restaurant1);
                                    mMarkerClickedCallback.onMarkerClickedGetRestaurant(restaurant1);
                                }
                            }
                        }

                        return false;
                    });
                }
            }
        private void sendRestaurantListToResponseCallback(ListAsyncResponse listResponseCallback){
                //if we're at the and of the json result (that's mean we've got all restaurants) AND we call for the restaurant list
                if (listResponseCallback != null) {
                    //then we set the markerClickListener
                    if (mMarkerClickedCallback != null && mGoogleMap != null) {
                        mGoogleMap.setOnMarkerClickListener(marker -> {
                            if (!Objects.equals(marker.getTag(), Constants.DEVICE_POSITION)){ //if the marker doesn't correspond to the device location
                                for (Restaurant restaurant1 : mRestaurantList) {
                                    if (restaurant1.getAddress().equals(marker.getTag())) {
                                        RestaurantSelectedApi.getInstance().setRestaurantSelected(restaurant1);
                                        mMarkerClickedCallback.onMarkerClickedGetRestaurant(restaurant1);
                                    }
                                }
                            }

                            return false;
                        });
                    }

                    //and we send the list
                    listResponseCallback.processFinished(mRestaurantList);
                }
            }


    /*private PlacesClient placesClient;
    private AutocompleteSessionToken sessionToken;
    private RectangularBounds bounds;
    private FindAutocompletePredictionsRequest predictionRequest;
    private List<Place.Field> placeFields;*/

    /*RestaurantBank.getInstance().getRestaurantList(placesClient, predictionRequest, placeFields, new RestaurantBank.ListAsyncResponse() {
        @Override
        public void processFinished(List<Place> restaurantList) {
//                restaurantAdapter = new RestaurantRecyclerViewAdapter(context, (Restaurant)restaurantList);

            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(restaurantAdapter);
        }
    });*/

    /*private void initializePlaces(){
        if (!Places.isInitialized())
            Places.initialize(Objects.requireNonNull(getContext()), getString(R.string.google_maps_key));

        placesClient = Places.createClient(Objects.requireNonNull(getContext()));
    }
    private void initializePredictionRequestAndPlaceFields(){
        sessionToken = AutocompleteSessionToken.newInstance();
        bounds = RectangularBounds.newInstance(LatLngBounds.builder().include(new LatLng(45.7757747, 3.0804423)).build());

        predictionRequest = FindAutocompletePredictionsRequest.builder()
                .setCountry("fr")
                .setLocationBias(bounds)
                .setTypeFilter(TypeFilter.GEOCODE)
                .setSessionToken(sessionToken)
                .build();

        placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG,
                Place.Field.PHOTO_METADATAS, Place.Field.OPENING_HOURS, Place.Field.PHONE_NUMBER, Place.Field.WEBSITE_URI, Place.Field.TYPES);
    }*/
}
