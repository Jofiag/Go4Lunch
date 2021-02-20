package com.example.go4lunch.fragment;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import androidx.fragment.app.Fragment;

import com.example.go4lunch.R;
import com.example.go4lunch.adapter.RestaurantRecyclerViewAdapter;
import com.example.go4lunch.model.Restaurant;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RestaurantMapViewFragment extends Fragment {

    private final OnMapReadyCallback callback = googleMap -> {
        setGoogleMap(googleMap);
    };

    private PlacesClient placesClient;
    private final List<String> placesSuggestion = new ArrayList<>();
    private RestaurantRecyclerViewAdapter restaurantAdapter;

    public RestaurantMapViewFragment() {
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
        setMapFragment();
        initializePlaces();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_view_menu, menu);
        setOurSearchView(menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkGooglePlayServices();
    }

    private void setGoogleMap(GoogleMap googleMap){
        LatLng sydney = new LatLng(-34, 151);
        googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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

    private void getPlaceEntered(String query){
        List<Restaurant> restaurantSuggestions = new ArrayList<>();

        AutocompleteSessionToken sessionToken = AutocompleteSessionToken.newInstance();
        RectangularBounds bounds = RectangularBounds.newInstance(new LatLng(45.7833, 3.0833), new LatLng(48.8534, 2.3488));
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setCountry("fr")
                .setLocationBias(bounds)
                .setTypeFilter(TypeFilter.ADDRESS)
                .setSessionToken(sessionToken)
                .setQuery(query)
                .build();

        placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener(response -> {
                    for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                        placesSuggestion.add(prediction.getPlaceId());
                        Restaurant restaurant = new Restaurant();
                        restaurant.setName((prediction.getFullText(null)).toString());
                        restaurantSuggestions.add(restaurant);
//                        Toast.makeText(getContext(), prediction.getFullText(null), Toast.LENGTH_SHORT).show();
                        Log.d("PLACE", "getPlaceEntered: " + prediction.getFullText(null));

                    }
                })
                .addOnFailureListener(e -> {
                    if (e instanceof ApiException)
                        Log.d("PLACE", "getPlaceEntered: " + e.getMessage());
                });

        restaurantAdapter = new RestaurantRecyclerViewAdapter(getContext(), restaurantSuggestions);
        restaurantAdapter.getFilter().filter(query);
    }

    private void setOurSearchView(Menu menu){
        MenuItem searchItem = menu.findItem(R.id.search_item);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(Constants.SEARCH_RESTAURANTS_TEXT);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Action after user validate his search text
                SearchRecentSuggestions searchRecentSuggestions = new SearchRecentSuggestions(getContext(), RestaurantSuggestions.AUTHORITY, RestaurantSuggestions.MODE);
                searchRecentSuggestions.saveRecentQuery(query, null);
                Toast.makeText(getContext(), query, Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Real time action
//                getPlaceEntered(newText);
                return false;
            }
        });

                // OR
//        Intent intent = getActivity().getIntent();
//        if (Intent.ACTION_SEARCH.equals(intent.getAction())){
//            String query = intent.getStringExtra(SearchManager.QUERY);
//            SearchRecentSuggestions searchRecentSuggestions = new SearchRecentSuggestions(getContext(), RestaurantSuggestions.AUTHORITY, RestaurantSuggestions.MODE);
//            searchRecentSuggestions.saveRecentQuery(query, null);
//            Toast.makeText(getActivity(), query, Toast.LENGTH_SHORT).show();
//        }
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