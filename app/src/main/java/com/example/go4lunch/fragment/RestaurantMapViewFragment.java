package com.example.go4lunch.fragment;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;

import com.example.go4lunch.R;
import com.example.go4lunch.data.RestaurantNearbyBank;
import com.example.go4lunch.model.Restaurant;
import com.example.go4lunch.util.Constants;
import com.example.go4lunch.util.RestaurantSuggestions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;
import java.util.Objects;

import static android.content.Context.LOCATION_SERVICE;
import static com.example.go4lunch.util.Constants.ADDRESS;
import static com.example.go4lunch.util.Constants.FINE_LOCATION;
import static com.example.go4lunch.util.Constants.NAME;
import static com.example.go4lunch.util.Constants.NEARBY_SEARCH_URL;
import static com.example.go4lunch.util.Constants.PROXIMITY_RADIUS;

@RequiresApi(api = Build.VERSION_CODES.M)
public class RestaurantMapViewFragment extends Fragment {
    private final OnMapReadyCallback callback;

    /*private PlacesClient placesClient;
    private AutocompleteSessionToken sessionToken;
    private RectangularBounds bounds;
    private FindAutocompletePredictionsRequest predictionRequest;
    private List<Place.Field> placeFields;*/

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location deviceLocation;
    private LatLng devicePosition;

    private int[] viewIds;
    private CursorAdapter adapter;
    private String[] columnPlaces;
    private SearchView searchView;
    private String url;

    private GoogleMap mGoogleMap;

    public RestaurantMapViewFragment() {
        callback = this::setGoogleMap;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ORDER", "onCreate: ");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        Log.d("ORDER", "onCreateView: ");
        return inflater.inflate(R.layout.fragment_restaurant_map_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("ORDER", "onViewCreated: ");

        if (savedInstanceState != null){
            url = savedInstanceState.getString("url");
            Parcelable mapS = savedInstanceState.getParcelable("map");
            GoogleMap map = (GoogleMap) mapS;
            showAllRestaurantNearby(map);
            Log.d("U2", "onViewCreated: " + savedInstanceState.getString("url"));
        }

        setLocationManagerAndListener();
        requestLocationIfPermissionIsGranted(null);
        initializeSearchViewNeeded();

//        initializePlaces();
//        initializePredictionRequestAndPlaceFields();
    }

    private void initializeSearchViewNeeded() {
        columnPlaces = new String[]{
                BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1, //The main line of a suggestion (necessary)
                SearchManager.SUGGEST_COLUMN_TEXT_2  //The second line for a secondary text (optional)
        };

        viewIds = new int[]{
                R.id.place_id,
                R.id.place_name,
                R.id.place_address
        };

        adapter = new SimpleCursorAdapter(requireContext(),
                R.layout.suggestion_list_row,
                null,
                columnPlaces,
                viewIds,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_view_menu, menu);
        setOurSearchView(menu);
        Log.d("ORDER", "onCreateOptionsMenu: ");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d("ORDER", "onAttach: ");
    }

    @Override
    public void onResume() {
        super.onResume();
        setMapFragment();
        checkGooglePlayServices();
        Log.d("ORDER", "onResume: ");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("STATE", "onSaveInstanceState: ");
        Log.d("ORDER", "onSaveInstanceState: ");

        outState.putString("url", url);
        outState.putParcelable("map", (Parcelable) mGoogleMap);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.d("STATE", "onViewStateRestored: ");
        Log.d("ORDER", "onViewStateRestored: ");

        if (savedInstanceState != null){
            url = savedInstanceState.getString("url");
            Parcelable mapS = savedInstanceState.getParcelable("map");
            GoogleMap map = (GoogleMap) mapS;
            showAllRestaurantNearby(map);
            Log.d("U2", "onViewCreated: " + savedInstanceState.getString("url"));
        }
    }

    private void setGoogleMap(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        requestLocationIfPermissionIsGranted(googleMap);
        Log.d("ORDER", "setGoogleMap: ");

        if (deviceLocation != null) {
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            addMarkerOnPosition(googleMap, devicePosition, "My position", BitmapDescriptorFactory.HUE_RED);
            url = getUrl(deviceLocation);
            showAllRestaurantNearby(googleMap);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(devicePosition, 11));
        }
        else
            Toast.makeText(getContext(), "Location not available !", Toast.LENGTH_SHORT).show();


        if (getQuerySearched() != null && columnPlaces != null && adapter != null) //If user search for a restaurant nearby
            ZoomOnRestaurantSearched(googleMap, getQuerySearched());               //then we zoom on that restaurant

    }
    private void setMapFragment(){
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null)
            mapFragment.getMapAsync(callback);
    }

//    private void initializePlaces(){
//        if (!Places.isInitialized())
//            Places.initialize(requireContext(), getString(R.string.google_maps_key));
//
//        placesClient = Places.createClient(requireContext());
//    }
//    private void initializePredictionRequestAndPlaceFields(){
//        sessionToken = AutocompleteSessionToken.newInstance();
//        bounds = RectangularBounds.newInstance(LatLngBounds.builder().include(devicePosition).build());
//
//        predictionRequest = FindAutocompletePredictionsRequest.builder()
//                .setCountry("fr")
//                .setLocationBias(bounds)
//                .setTypeFilter(TypeFilter.GEOCODE)
//                .setSessionToken(sessionToken)
//                .build();
//
//        placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG,
//                Place.Field.PHOTO_METADATAS, Place.Field.OPENING_HOURS, Place.Field.PHONE_NUMBER, Place.Field.WEBSITE_URI, Place.Field.TYPES);
//    }
    /*private void getPlaceEntered(String query, GoogleMap googleMap, String[] columnPlaces, CursorAdapter adapter){
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
                                LatLng restaurantPosition = restaurant.getLatLng();
                                //Zooming on the restaurant clicked
                                if (restaurantPosition != null && Objects.equals(restaurant.getAddress(), getFromQuery(query, ADDRESS))) {
                                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(restaurantPosition, 17));
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
    }*/

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
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(Constants.SEARCH_RESTAURANTS_TEXT);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
        searchView.setIconifiedByDefault(false);

        /*String[] SUGGESTIONS = {
                "Pizza",
                "Burger",
                "Salad",
                "Rice"
        };*/

        columnPlaces = new String[]{
                BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1, //The main line of a suggestion (necessary)
                SearchManager.SUGGEST_COLUMN_TEXT_2  //The second line for a secondary text (optional)
        };

        viewIds = new int[]{
                R.id.place_id,
                R.id.place_name,
                R.id.place_address
        };

        adapter = new SimpleCursorAdapter(requireContext(),
                R.layout.suggestion_list_row,
                null,
                columnPlaces,
                viewIds,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        searchView.setSuggestionsAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Show suggestion
                showSuggestions(newText);

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

    }

    private LatLng getPositionFromLocation(Location location){
        if(location != null)
            return new LatLng(location.getLatitude(), location.getLongitude());
        return null;
    }
    private void addMarkerOnPosition(GoogleMap googleMap, LatLng position, String title, float color){
        googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(color)));
    }
    /*private BitmapDescriptor getBitmapFromVectorAssets(Context context, int id){
        Drawable vectorDrawable = ContextCompat.getDrawable(context, id);
        assert vectorDrawable != null;
        vectorDrawable.setBounds(0,0,vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }*/


    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> {
                if (result)
                    requestLocationIfPermissionIsGranted(null);
                else
                    requestPermissionWithinDialog();
            }
    );
    private void setLocationManagerAndListener(){
        locationManager = (LocationManager) requireContext().getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                deviceLocation = location;
                devicePosition = getPositionFromLocation(location);

                Log.d("LOCATION", "onLocationChanged: " + devicePosition);

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
    private void requestLocationIfPermissionIsGranted(GoogleMap googleMap) {
        if (requireContext().checkSelfPermission(FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);
            deviceLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            devicePosition = getPositionFromLocation(deviceLocation);

            if (googleMap != null) {
                googleMap.setMyLocationEnabled(true);
            }
        }
        else{
            if (shouldShowRequestPermissionRationale(FINE_LOCATION))
                Toast.makeText(getContext(), "Location permission is required", Toast.LENGTH_SHORT).show();

            requestPermissionLauncher.launch(FINE_LOCATION);
        }

    }
    private void requestPermissionWithinDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Location permission disable")
                .setMessage("You denied the location permission. It is required to show your location. Do you want to grant the permission")
                .setPositiveButton("YES", (dialog, which) -> requestLocationIfPermissionIsGranted(null))
                .setNegativeButton("NO", null)
                .create()
                .show();
    }

    private void checkGooglePlayServices(){
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(requireContext());

        if (resultCode != ConnectionResult.SUCCESS){
            Dialog googleErrorDialog = GoogleApiAvailability.getInstance().getErrorDialog(requireActivity(), resultCode, resultCode,
                    dialog -> {
                        //Here is what we're going to show to the user if the connection has canceled
                        Toast.makeText(getContext(), "No Google services!!!", Toast.LENGTH_SHORT).show();
                    });
            assert googleErrorDialog != null;
            googleErrorDialog.show();
        }
        else
            Log.d("SERVICES", "checkGooglePlayServices: Google services successfully connected!");
    }

 ////////////////////////////////////////////////////////////////////////////// USING JSON //////////////////////////////////////////////////////////////////////////////
    private void showAllRestaurantNearby(GoogleMap googleMap){
        RestaurantNearbyBank.getInstance(getContext(), googleMap).getRestaurantNearbyList(url, true, restaurantList -> {

        });
    }

    private void ZoomOnRestaurantSearched(GoogleMap googleMap, String query){
        if (googleMap != null){
            googleMap.clear();                  // Removing all marker added
            url = getUrl(deviceLocation);      // Getting url to get information about nearby restaurant on google maps.

            RestaurantNearbyBank.getInstance(getContext(), googleMap).getRestaurantNearbyList(url, false, restaurantList -> {

                for (Restaurant restaurant : restaurantList) {
                    LatLng restaurantPosition = restaurant.getPosition();
                    //Zooming on the restaurant clicked
                    if (restaurantPosition != null && Objects.equals(restaurant.getAddress(), getFromQuery(query, ADDRESS))) {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(restaurantPosition, 17));
                        addMarkerOnPosition(googleMap, restaurantPosition, restaurant.getName(), BitmapDescriptorFactory.HUE_ORANGE);
                    }

                    getFromQuery(query, NAME);
                }

            });
        }
    }

    private String getUrl(Location location){
        String url = null;

        if (location != null){
            url = NEARBY_SEARCH_URL +
                    "location=" + location.getLatitude() + "," + location.getLongitude() +
                    "&radius=" + PROXIMITY_RADIUS +
                    "&type=" + Constants.RESTAURANT +
                    "&sensor=true" +
                    "&key=" + getString(R.string.google_maps_key);
        }

        Log.d("URL", "getUrl: " + url);

        return url;
    }

    private void showSuggestions(String query){
        RestaurantNearbyBank.getInstance(getContext(), null).getRestaurantNearbyList(url, false, restaurantList -> {
            if (columnPlaces != null && adapter != null && query != null) {
                Log.d("SEARCH", "getRestaurantNearby: columnPlaces != null && adapter != null && query != null");
                //When we've got all the restaurant
                if (!restaurantList.isEmpty()) {
                    //Then we add all of them to our cursor to show it as suggestions to the user
                    final MatrixCursor cursor = new MatrixCursor(columnPlaces);
                    int y = 0;
                    for (Restaurant restaurant : restaurantList) {
                        if (restaurant != null && restaurant.getName() != null) {
                            String placeName = restaurant.getName();
                            if (placeName.toLowerCase().contains(query.toLowerCase()))
                                cursor.addRow(new Object[]{y, placeName, restaurant.getAddress()});
                            Log.d("SEARCH", "getRestaurantNearby: " + placeName);
                        }
                        y++;
                    }

                    adapter.changeCursor(cursor);
                }
                if (searchView != null) {
                    Log.d("SEARCH", "getRestaurantNearby: searchView != null");
                    searchView.setSuggestionsAdapter(adapter);
                }
                Log.d("SEARCH", "onQueryTextChange: " + query);
                Log.d("SEARCH", "onQueryTextChange: " + adapter.getCount());
            }
        });
    }
}