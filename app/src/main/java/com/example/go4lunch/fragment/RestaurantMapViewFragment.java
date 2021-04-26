package com.example.go4lunch.fragment;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;

import com.example.go4lunch.R;
import com.example.go4lunch.util.Constants;
import com.example.go4lunch.util.RestaurantSuggestions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class RestaurantMapViewFragment extends Fragment {

    private final OnMapReadyCallback callback;

    private PlacesClient placesClient;
    private AutocompleteSessionToken sessionToken;
    private RectangularBounds bounds;
    private FindAutocompletePredictionsRequest predictionRequest;
    private List<Place.Field> placeFields;

    public RestaurantMapViewFragment() {
        callback = this::setGoogleMap;

        /*callback = new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                setGoogleMap(googleMap);
            }
        };*/
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        return inflater.inflate(R.layout.fragment_restaurant_map_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializePlaces();
        initializePredictionRequestAndPlaceFields();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_view_menu, menu);
        setOurSearchView(menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        setMapFragment();
        checkGooglePlayServices();
    }

    private void setGoogleMap(GoogleMap googleMap){
        LatLng jaude = new LatLng(45.7757747, 3.0804423);
        googleMap.addMarker(new MarkerOptions().position(jaude).title("Jaude Clermont-Ferrand"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(jaude, 18));
        getPlaceEntered(getQuerySearched(), googleMap, null, null);
//        googleMap.setMyLocationEnabled(true);
//        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
//        addCustomMarkerOnRestaurantPosition(googleMap);
    }

    private void setMapFragment(){
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null)
            mapFragment.getMapAsync(callback);
    }

    private void initializePlaces(){
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
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setSessionToken(sessionToken)
                .build();

        placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG,
                Place.Field.PHOTO_METADATAS, Place.Field.OPENING_HOURS, Place.Field.PHONE_NUMBER, Place.Field.WEBSITE_URI, Place.Field.TYPES);
    }

    /*private void addCustomMarkerOnRestaurantPosition(GoogleMap googleMap){
        placesClient.findAutocompletePredictions(predictionRequest)
                .addOnSuccessListener(response -> {
                    for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                        FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(prediction.getPlaceId(), placeFields).build();

                        placesClient.fetchPlace(placeRequest).addOnSuccessListener(fetchPlaceResponse -> {
                            Place place = fetchPlaceResponse.getPlace();
                            if (Objects.requireNonNull(place.getTypes()).contains(Place.Type.RESTAURANT)) {

                                    //Add personal marker on restaurant
                                    googleMap.addMarker(new MarkerOptions()
                                            .position(Objects.requireNonNull(place.getLatLng()))
                                            .title(place.getName())
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

                            }
                        });
                    }

                });
    }*/

    private void getPlaceEntered(String query, GoogleMap googleMap, String[] columnPlaces, CursorAdapter adapter){
        predictionRequest = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setCountry("fr")
                .setLocationBias(bounds)
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setSessionToken(sessionToken)
                .build();

        placesClient.findAutocompletePredictions(predictionRequest)
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
                                    String placeNameInLowercase = place.getName().toLowerCase();
                                    String queryInLowercase = query.toLowerCase();

                                    if (placeNameInLowercase.equals(queryInLowercase))
                                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 18));
                                    else
                                        Toast.makeText(getContext(), place.getName() + place.getTypes(), Toast.LENGTH_SHORT).show();
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
                });
    }

    private String getQuerySearched(){
        Intent intent = Objects.requireNonNull(getActivity()).getIntent();
        SearchRecentSuggestions searchRecentSuggestions = new SearchRecentSuggestions(getContext(),
                RestaurantSuggestions.AUTHORITY,
                RestaurantSuggestions.MODE);

        String query = null;
//        searchRecentSuggestions.clearHistory();

        if (Intent.ACTION_SEARCH.equals(intent.getAction())){
            query = intent.getStringExtra(SearchManager.QUERY);
            searchRecentSuggestions.saveRecentQuery(query, null);

        }

        return query;
    }

    private void setOurSearchView(Menu menu){
        MenuItem searchItem = menu.findItem(R.id.search_item);

        SearchManager searchManager = (SearchManager) Objects.requireNonNull(getActivity()).getSystemService(Context.SEARCH_SERVICE);
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

        CursorAdapter adapter = new SimpleCursorAdapter(getContext(),
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
                //when user click on a restaurant suggested, set searchView query with the restaurant clicked name
                Cursor cursor = (Cursor) adapter.getItem(position);
                String text = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
                searchView.setQuery(text, true);
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

    private void checkGooglePlayServices(){
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
    }

//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        List<Place.Field> fieldList = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.PHOTO_METADATAS, Place.Field.OPENING_HOURS);
//        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(Objects.requireNonNull(getContext()));
//
//        if (item.getItemId() == R.id.search_place_item)
//            startActivityForResult(intent, Constants.AUTOCOMPLETE_REQUEST_CODE);
//
//        return true;
//    }
//
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        if (requestCode == Constants.AUTOCOMPLETE_REQUEST_CODE){
//            if (resultCode == RESULT_OK){
//                Place place = Autocomplete.getPlaceFromIntent(Objects.requireNonNull(data));
//                Toast.makeText(getContext(), place.getName(), Toast.LENGTH_SHORT).show();
//            }
//            else if (resultCode == AutocompleteActivity.RESULT_ERROR){
//                Status status = Autocomplete.getStatusFromIntent(Objects.requireNonNull(data));
//                Toast.makeText(getContext(), status.getStatusMessage(), Toast.LENGTH_SHORT).show();
//            }
//            return;
//        }
//
//        super.onActivityResult(requestCode, resultCode, data);
//    }
}