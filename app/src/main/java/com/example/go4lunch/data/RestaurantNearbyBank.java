package com.example.go4lunch.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

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
    @SuppressLint("StaticFieldLeak")
    private static RestaurantNearbyBank INSTANCE;

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
        if (INSTANCE == null) {
            if (googleMap == null)
                INSTANCE = new RestaurantNearbyBank(context);
            else
                INSTANCE = new RestaurantNearbyBank(context, googleMap);
        }

        return INSTANCE;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void getRestaurantNearbyList(String url, final ListAsyncResponse listResponseCallback){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d("URL", "getRestaurantNearbyList: " + url);
                    try {
                        /*if (!mRestaurantList.isEmpty())
                            mRestaurantList = new ArrayList<>();*/

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


                                String placeId = resultObject.getString(Constants.PLACE_ID);
                                String address = getStreetAddressFromPositions(position);

                                Restaurant restaurant = new Restaurant();
                                restaurant.setName(name);
                                restaurant.setAddress(address);
                                restaurant.setPosition(position);
                                restaurant.setPlaceId(placeId);
                                restaurant.setFavorableOpinion(favorableOpinion);
                                if (!photoReference.equals(""))
                                    restaurant.setImageUrl(photoUrl);
//                                Log.d("DETAILS", "getRestaurantNearbyList: ID = " + placeId);
//                                Log.d("DETAILS", "getRestaurantNearbyList: PLACES = " + url);

                                setMoreRestaurantDetails(restaurant, placeId, listResponseCallback);

                                mRestaurantList.add(restaurant);

                                if (mGoogleMap != null)
                                    addMarkerOnPosition(mGoogleMap, position, name, restaurantIndex);

                                restaurantIndex++;
                            }

                        }

                        /*if (listResponseCallback != null)
                            listResponseCallback.processFinished(mRestaurantList);*/

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setMoreRestaurantDetails(Restaurant restaurant, String placeId, ListAsyncResponse listResponseCallback){
        /*String detailsUrl = Constants.PLACE_DETAILS_SEARCH_URL +
                "place_id=" + placeId +
                "&key=" + mContext.getString(R.string.google_maps_key);*/

//        Log.d("DETAILS", "getOpeningHours: DETAILS = " + detailsUrl);

        if(!Places.isInitialized())
            Places.initialize(Objects.requireNonNull(mContext), mContext.getString(R.string.google_maps_key));

        PlacesClient placesClient = Places.createClient(Objects.requireNonNull(mContext));
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.OPENING_HOURS, Place.Field.WEBSITE_URI, Place.Field.PHONE_NUMBER, Place.Field.UTC_OFFSET);
        FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.newInstance(placeId, placeFields);

        if (!mRestaurantList.isEmpty())
            mRestaurantList = new ArrayList<>();

        placesClient.fetchPlace(fetchPlaceRequest)
                .addOnSuccessListener(response -> {
                    Place place = response.getPlace();

                    OpeningHours openingHours = place.getOpeningHours();
                    String phoneNumber = place.getPhoneNumber();
                    Uri website = place.getWebsiteUri();
                    String name = place.getName();

                    String currentDayOfWeek = LocalDate.now().getDayOfWeek().toString();
                    MyOpeningHours myOpeningHours = new MyOpeningHours();

                    if (openingHours != null){
                        for (int i = 0; i < openingHours.getPeriods().size(); i++) {
                                Period period = openingHours.getPeriods().get(i);
                                Period nextPeriod = openingHours.getPeriods().get(i+1);
                                String openDay = Objects.requireNonNull(period.getOpen()).getDay().toString();
                                String nextOpenDay = Objects.requireNonNull(nextPeriod.getOpen()).getDay().toString();

                                if (openDay.equals(currentDayOfWeek)){
                                    if (nextOpenDay.equals(currentDayOfWeek)) {

                                        myOpeningHours.setFirstOpeningTime(period.getOpen().getTime());
                                        myOpeningHours.setFirstClosingTime(Objects.requireNonNull(period.getClose()).getTime());
                                        myOpeningHours.setLastOpeningTime(nextPeriod.getOpen().getTime());
                                        myOpeningHours.setLastClosingTime(Objects.requireNonNull(nextPeriod.getClose()).getTime());

                                    }
                                    else {

                                        myOpeningHours.setFirstOpeningTime(period.getOpen().getTime());
                                        myOpeningHours.setFirstClosingTime(Objects.requireNonNull(period.getClose()).getTime());
                                    }

                                    i = openingHours.getPeriods().size();   //Stopping the for loop
                                }
                            }
                    }

                    if (openingHours != null && name != null && name.toLowerCase().equals("la brasserie bordelaise"))
                        Log.d("DETAILS", "setMoreRestaurantDetails: PERIODS =  " + openingHours.getPeriods());
//                            Log.d("DETAILS", "setMoreRestaurantDetails: PERIODS =  " + openingHours.getWeekdayText());

//                    Log.d("DETAILS", "getOpeningHours: \n NAME = " + name);
//                    Log.d("DETAILS", "getOpeningHours: \n WEBSITE = " + website);
//                    Log.d("DETAILS", "getOpeningHours: \n PHONE NUMBER = " + phoneNumber);
//                    Log.d("DETAILS", "getOpeningHours: \n OPENING HOURS = " + openingHours);

                    restaurant.setPhoneNumber(phoneNumber);
                    restaurant.setWebsiteUrl(website);
                    restaurant.setOpeningHours(myOpeningHours);

                    mRestaurantList.add(restaurant);

                    if (listResponseCallback != null)
                        listResponseCallback.processFinished(mRestaurantList);
                });

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
