package com.example.go4lunch.fragment;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;

import com.example.go4lunch.R;
import com.example.go4lunch.data.RestaurantBank;
import com.example.go4lunch.util.Constants;
import com.example.go4lunch.util.RestaurantSuggestions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static android.content.Context.LOCATION_SERVICE;

@RequiresApi(api = Build.VERSION_CODES.M)
public class RestaurantMapViewFragment extends Fragment {
    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final int REQUEST_LOCATION_PERMISSION_CODE = 5;
    public static final String NAME = "name";
    public static final String ADDRESS = "address";
    public static final String RESTAURANT_CLICKED_POSITION = "position";

    private final OnMapReadyCallback callback;

    private PlacesClient placesClient;
    private AutocompleteSessionToken sessionToken;
    private RectangularBounds bounds;
    private FindAutocompletePredictionsRequest predictionRequest;
    private List<Place.Field> placeFields;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location deviceLocation;

    public RestaurantMapViewFragment() {
        callback = this::setGoogleMap;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        Log.d("ORDER", "onCreateView: ");
        return inflater.inflate(R.layout.fragment_restaurant_map_view, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("ORDER", "onViewCreated: ");

        setLocationManagerAndListener();
        requestLocationIfPermissionIsGranted();

        initializePlaces();
        initializePredictionRequestAndPlaceFields();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_view_menu, menu);
        Log.d("ORDER", "onCreateOptionsMenu: ");
        setOurSearchView(menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("ORDER", "onResume: ");
        setMapFragment();
//        checkGooglePlayServices();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setGoogleMap(GoogleMap googleMap){

        if (deviceLocation != null){
            LatLng position = new LatLng(deviceLocation.getLatitude(), deviceLocation.getLongitude());
            addMarkerOnPosition(googleMap, position, "My position", BitmapDescriptorFactory.HUE_RED);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 17));
        }

        Runnable addOrangeMarkerToRestaurant = () -> {
            try {
                //Adding customized marker on restaurant nearby position
                RestaurantBank.getInstance().getRestaurantList(placesClient, predictionRequest, placeFields, restaurantList -> {
                    for (int i = 0; i < restaurantList.size(); i++) {
                        Place restaurant = restaurantList.get(i);
                        addMarkerOnPosition(googleMap, restaurant.getLatLng(), restaurant.getName(), BitmapDescriptorFactory.HUE_ORANGE);
                    }
                });

                getPlaceEntered(getQuerySearched(), googleMap, null, null);
            }
            catch (Exception e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        Thread secondThread = new Thread(addOrangeMarkerToRestaurant);
        secondThread.start();


//        googleMap.setMyLocationEnabled(true);
//        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
    }
    private void setMapFragment(){
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null)
            mapFragment.getMapAsync(callback);
    }

    private void initializePlaces(){
        if (!Places.isInitialized())
            Places.initialize(requireContext(), getString(R.string.google_maps_key));

        placesClient = Places.createClient(requireContext());
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
    }
    private void getPlaceEntered(String query, GoogleMap googleMap, String[] columnPlaces, CursorAdapter adapter){
        if (query != null)
            predictionRequest = FindAutocompletePredictionsRequest.builder()
                    .setQuery(getFromQuery(query, NAME))
                    .setCountry("fr")
                    .setLocationBias(bounds)
                    .setTypeFilter(TypeFilter.ESTABLISHMENT)
                    .setSessionToken(sessionToken)
                    .build();


        RestaurantBank.getInstance().getRestaurantList(placesClient, predictionRequest, placeFields,
                restaurantList -> {
                    if (googleMap != null){
//                        int position = Integer.parseInt(getFromQuery(query, RESTAURANT_CLICKED_POSITION));

                            for (Place restaurant : restaurantList) {
                                //Zooming on the restaurant clicked
                                if (Objects.equals(restaurant.getAddress(), getFromQuery(query, ADDRESS))) {
                                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(restaurant.getLatLng(), 17));
                                    addMarkerOnPosition(googleMap, restaurant.getLatLng(), restaurant.getName(), BitmapDescriptorFactory.HUE_ORANGE);
                                }
                            }
                    }

                    if (columnPlaces != null && adapter != null){
                        //When we've got all the restaurant
                        if (!restaurantList.isEmpty()){

                            //Then we add all of them to our cursor to show it as suggestions to the user
                            Log.d("LIST", "onQueryTextChange: " + restaurantList.size() + restaurantList);
                            final MatrixCursor cursor = new MatrixCursor(columnPlaces);
                            int y = 0;
                            for (Place placeSuggested : restaurantList) {
                                if (placeSuggested != null && placeSuggested.getName() != null && query != null) {
                                    String placeName = placeSuggested.getName();
                                    if (placeName.toLowerCase().contains(query.toLowerCase()))
                                        cursor.addRow(new Object[]{y, placeName, placeSuggested.getAddress()});
                                }
                                y++;
                            }

                            adapter.changeCursor(cursor);
                        }
                    }
                });

        /*placesClient.findAutocompletePredictions(predictionRequest)
                .addOnSuccessListener(response -> {
                    int i = 0;
                    List<Place> placeList = new ArrayList<>();
                    List<AutocompletePrediction> predictionList = response.getAutocompletePredictions();

                    for (AutocompletePrediction prediction : predictionList) {
                        FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(prediction.getPlaceId(), placeFields).build();

                        int finalI = i;
                        placesClient.fetchPlace(placeRequest).addOnSuccessListener(fetchPlaceResponse -> {
                            Place place = fetchPlaceResponse.getPlace();
                            if (Objects.requireNonNull(place.getTypes()).contains(Place.Type.RESTAURANT)) {
                                //Adding a place witch is a restaurant in our list
                                placeList.add(place);

                                //Zooming on the query submitted
                                if (googleMap != null){
                                    String placeAddressInLowercase = place.getAddress().toLowerCase();
                                    String queryAddressInLowercase = getFromQuery(query, ADDRESS).toLowerCase();

                                    //Adding marker on restaurant
                                    addMarkerOnRestaurant(googleMap, place.getLatLng(), place.getName(), BitmapDescriptorFactory.HUE_ORANGE);

                                    //Zooming on restaurant searched
                                    if (placeAddressInLowercase.equals(queryAddressInLowercase))
                                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 20));
                                }

                                if (columnPlaces != null && adapter != null){
                                    //When we've got all the restaurant
                                    if (finalI == response.getAutocompletePredictions().size()-1){
                                        if (!placeList.isEmpty()){

                                            //Then we add all of them to our cursor to show it as suggestions to the user
                                            Log.d("LIST", "onQueryTextChange: " + placeList.size() + placeList);
                                            final MatrixCursor cursor = new MatrixCursor(columnPlaces);
                                            int y = 0;
                                            for (Place placeSuggested : placeList) {
                                                if (placeSuggested != null) {
                                                    String placeName = placeSuggested.getName();
                                                    if (placeName.toLowerCase().contains(query.toLowerCase()))
                                                        cursor.addRow(new Object[]{y, placeName, placeSuggested.getAddress()});
                                                }
                                                y++;
                                            }

                                            adapter.changeCursor(cursor);
                                        }
                                    }
                                }
                            }

                        });

                        i++;
                    }

                })
                .addOnFailureListener(e -> {
                    if (e instanceof ApiException)
                        Log.d("SUGGESTIONS", "getPlaceEntered: " + e.getMessage());
                });*/
    }

    private String getFromQuery(String query, String wanted){
        String result = null;
        char[] resultArray;

        if (query != null){
            resultArray = new char[query.length()];


            //If we want the NAME
            if (wanted.equals(NAME)){
                for (int i = 0; i < query.length(); i++){
                    resultArray[i] = query.charAt(i);                       //We add each query character

                    if (i+1 < query.length() && query.charAt(i+1) == '_')   //until the next character is '_'
                        i = query.length()-1;                               //then, we stop adding.
                }
            }
            //If we want the address
            else if (wanted.equals(ADDRESS)){
                int y = 0, z = 0;                        //We'll use y to save a position and z to re-initialize the resultArray from it's first element

                for (int i = 0; i < query.length(); i++){
                    if (query.charAt(i) == '_')                             //We find the character '_'
                        y = i;                                              //then, we save it's position in y.

                    if (y != 0 && i > y) {                                      //When we'r at the character '_' position,
                        resultArray[z] = query.charAt(i);                       //we save all the character after that position

                        if (i+1 < query.length() && query.charAt(i+1) == '/')   //until the next character is '/'
                            i = query.length()-1;                               //then, we stop adding

                        z++;                                                    //If the next character isn't '/', we continue adding.
                    }
                }
            }
            //If we want the restaurant clicked position
            else{
                int y = 0, z = 0;                         //We'll use y to save a position and z to re-initialize the resultArray from it's first element

                for (int i = 0; i < query.length(); i++) {
                    if (query.charAt(i) == '/')             //When we reach the character '/',
                        y = i;                              //we save it's position

                    if (y != 0 && i > y){                   //When we're at the character '/' position,
                        resultArray[z] = query.charAt(i);   //we save all the character after that position.
                        z++;
                    }
                }
                result = new String(resultArray).trim();
                Log.d("GETNA", "getFromQuery: " + Integer.parseInt(result));

            }

            result = new String(resultArray).trim();

            Log.d("GETNA", "getFromQuery: " + result);
        }

        return result;
    }
    private String getQuerySearched(){
        Intent intent = requireActivity().getIntent();
        SearchRecentSuggestions searchRecentSuggestions = new SearchRecentSuggestions(getContext(),
                RestaurantSuggestions.AUTHORITY,
                RestaurantSuggestions.MODE);

        String query = null;

        if (Intent.ACTION_SEARCH.equals(intent.getAction())){
            query = intent.getStringExtra(SearchManager.QUERY);
            searchRecentSuggestions.saveRecentQuery(query, null);

        }

        return query;
    }
    private void setOurSearchView(Menu menu){
        MenuItem searchItem = menu.findItem(R.id.search_item);

        SearchManager searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(Constants.SEARCH_RESTAURANTS_TEXT);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
//        searchView.setIconifiedByDefault(false);

        /*String[] SUGGESTIONS = {
                "Pizza",
                "Burger",
                "Salad",
                "Rice"
        };*/

        String[] columnPlaces = {
                BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1, //The main line of a suggestion (necessary)
                SearchManager.SUGGEST_COLUMN_TEXT_2  //The second line for a secondary text (optional)
        };

        int[] viewIds = {
                R.id.place_id,
                R.id.place_name,
                R.id.place_address
        };

        CursorAdapter adapter = new SimpleCursorAdapter(requireContext(),
                R.layout.suggestion_list_row,
                null,
                columnPlaces,
                viewIds,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        searchView.setSuggestionsAdapter(adapter);

        /*searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                final MatrixCursor cursor = new MatrixCursor(columnPlaces);
                int i = 0;

                for (String suggestion : SUGGESTIONS) {
                    if (suggestion.toLowerCase().startsWith(newText.toLowerCase()))
                        cursor.addRow(new Object[]{i, suggestion, suggestion});
                    i++;
                }

                adapter.changeCursor(cursor);

                return false;
            }
        });*/

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Show suggestion
                getPlaceEntered(newText, null, columnPlaces, adapter);

                /*predictionRequest = FindAutocompletePredictionsRequest.builder()
                        .setQuery(newText)
                        .setCountry("fr")
                        .setLocationBias(bounds)
                        .setTypeFilter(TypeFilter.ESTABLISHMENT)
                        .setSessionToken(sessionToken)
                        .build();

                placesClient.findAutocompletePredictions(predictionRequest)
                        .addOnSuccessListener(response -> {
                            int i = 0;
                            List<AutocompletePrediction> predictionList = response.getAutocompletePredictions();

                            List<Place> placeList = new ArrayList<>();

                            for (AutocompletePrediction prediction : predictionList) {
                                FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(prediction.getPlaceId(), placeFields).build();

                                int finalI = i;
                                placesClient.fetchPlace(placeRequest).addOnSuccessListener(fetchPlaceResponse -> {
                                    Place place = fetchPlaceResponse.getPlace();
//                                    Place[] placeArray;

                                    if (Objects.requireNonNull(place.getTypes()).contains(Place.Type.RESTAURANT))
                                        placeList.add(place);

                                    if (finalI == predictionList.size()-1) {

                                        if (!placeList.isEmpty()){
                                            Log.d("LIST", "onQueryTextChange: " + placeList.size() + placeList);
                                            final MatrixCursor cursor = new MatrixCursor(columnPlaces);
                                            int y = 0;
                                            for (Place placeSuggested : placeList) {
                                                if (placeSuggested != null) {
                                                    String placeName = placeSuggested.getName();
                                                    if (placeName.toLowerCase().startsWith(newText.toLowerCase()))
                                                        cursor.addRow(new Object[]{y, placeName, placeSuggested.getAddress()});
                                                }
                                                y++;
                                            }

                                            adapter.changeCursor(cursor);
                                        }
                                    }
                                });

                                i++;
                            }

                        });*/

                ////////////////////////////////////////////////////////////////////////////////////
                /*final MatrixCursor cursor = new MatrixCursor(columnPlaces);
                int i = 0;
                for (String suggestion : SUGGESTIONS) {
                    if (suggestion.toLowerCase().startsWith(newText.toLowerCase()))
                        cursor.addRow(new Object[]{i, suggestion, suggestion});
                    i++;
                }

                adapter.changeCursor(cursor);*/

                return false;
            }
        });

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                //when user click on a restaurant suggested, set searchView query with the restaurant clicked address
                Cursor cursor = (Cursor) adapter.getItem(position);
                String placeName = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
                String placeAddress = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2));

                //We're setting both name and address as query,
                // because there can be many restaurant with the same name but never with the same address.
                //Then we can use the address to search for the restaurant in the map.
                searchView.setQuery(placeName + "_" + placeAddress + "/" + position, true);
                searchView.setSaveEnabled(true);

                return true;
            }
        });


//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                //Action after user validate his search text
//                SearchRecentSuggestions searchRecentSuggestions = new SearchRecentSuggestions(getContext(), RestaurantSuggestions.AUTHORITY, RestaurantSuggestions.MODE);
//                searchRecentSuggestions.saveRecentQuery(query, null);
//                Toast.makeText(getActivity(), query , Toast.LENGTH_SHORT).show();
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                //Real time action
////                getPlaceEntered(newText);
//                return false;
//            }
//        });
        /*// OR
        if (getQuerySearched() != null)
            getPlaceEntered(getQuerySearched(), null, null, null);*/
    }

    private void addMarkerOnPosition(GoogleMap googleMap, LatLng position, String title, float color){
        //Adding marker to map
        /* Marker marker = */googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(color)));
//                .flat(true)
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.orange_restaurant)));
//                .icon(getBitmapFromVectorAssets(getContext(), R.drawable.green_restaurant_24x24pp)));

//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(position));
    }
    private BitmapDescriptor getBitmapFromVectorAssets(Context context, int id){
        Drawable vectorDrawable = ContextCompat.getDrawable(context, id);
        vectorDrawable.setBounds(0,0,vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    private void setLocationManagerAndListener(){
        locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                deviceLocation = location;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestLocationIfPermissionIsGranted() {
        if (getContext().checkSelfPermission(FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, locationListener);
            deviceLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        else{
            if (shouldShowRequestPermissionRationale(FINE_LOCATION))
                Toast.makeText(getContext(), "Location permission is required", Toast.LENGTH_SHORT).show();

            requestPermissionLauncher.launch(FINE_LOCATION);
        }

    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> {
                if (result)
                    requestLocationIfPermissionIsGranted();
                else
                    requestPermissionWithinDialog();
            }
    );

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermissionWithinDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Location permission disable")
                .setMessage("You denied the location permission. It is required to show your location. Do you want to grant the permission")
                .setPositiveButton("YES", (dialog, which) -> requestLocationIfPermissionIsGranted())
                .setNegativeButton("NO", null)
                .create()
                .show();
    }

    /*private void checkGooglePlayServices(){
        int errorCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getContext());

        if (errorCode != ConnectionResult.SUCCESS){
            Dialog googleErrorDialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), errorCode, errorCode,
                    dialog -> {
                        //Here is what we're going to show to the user if the connection has canceled
                        Toast.makeText(getContext(), "No services", Toast.LENGTH_SHORT).show();
                    });
            googleErrorDialog.show();
        }
        else
            Toast.makeText(getContext(), "Google Play services connected", Toast.LENGTH_SHORT).show();
    }*/
}