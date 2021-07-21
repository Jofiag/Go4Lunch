package com.example.go4lunch.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.go4lunch.R;
import com.example.go4lunch.adapter.RestaurantRecyclerViewAdapter;
import com.example.go4lunch.data.RestaurantListUrlApi;
import com.example.go4lunch.data.RestaurantNearbyBank;
import com.example.go4lunch.model.Restaurant;
import com.example.go4lunch.util.Constants;
import com.example.go4lunch.util.LoadingDialog;

import java.util.ArrayList;
import java.util.List;

public class RestaurantListViewFragment extends Fragment{
    /*private PlacesClient placesClient;
    private AutocompleteSessionToken sessionToken;
    private RectangularBounds bounds;
    private FindAutocompletePredictionsRequest predictionRequest;
    private List<Place.Field> placeFields;*/

    private RestaurantRecyclerViewAdapter restaurantAdapter;

    private String url;
    private Activity activity;

    private RecyclerView recyclerView;

    public RestaurantListViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

        activity = getActivity();
        url = RestaurantListUrlApi.getInstance(getActivity()).getUrlThroughDeviceLocation();

        /*initializePlaces();
        initializePredictionRequestAndPlaceFields();*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_list_view, container, false);

        recyclerView = view.findViewById(R.id.restaurant_list_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        LoadingDialog dialog  = LoadingDialog.getInstance(getActivity());

        dialog.startLoadingDialog();
        RestaurantNearbyBank.getInstance(activity, null).getRestaurantNearbyList(url, restaurantList -> {
            restaurantAdapter = new RestaurantRecyclerViewAdapter(activity, restaurantList);
            recyclerView.setAdapter(restaurantAdapter);
            restaurantAdapter.notifyDataSetChanged();
            dialog.dismissLoadingDialog();
        });

        /*RestaurantBank.getInstance().getRestaurantList(placesClient, predictionRequest, placeFields, new RestaurantBank.ListAsyncResponse() {
            @Override
            public void processFinished(List<Place> restaurantList) {
//                restaurantAdapter = new RestaurantRecyclerViewAdapter(context, (Restaurant)restaurantList);

                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                recyclerView.setAdapter(restaurantAdapter);
            }
        });*/

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_view_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search_item);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setQueryHint(Constants.SEARCH_RESTAURANTS_TEXT);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Action after user validate his search text
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Real time action
                /*if (restaurantAdapter != null)
                    restaurantAdapter.getFilter().filter(newText);*/

                filterList(newText);

                return false;
            }
        });

    }

    private void filterList(String query){
        RestaurantNearbyBank.getInstance(activity, null).getRestaurantNearbyList(url, restaurantList -> {
            List<Restaurant> listFiltered = new ArrayList<>();

            for (Restaurant restaurant : restaurantList)
                if (restaurant.getName().toLowerCase().contains(query.toLowerCase()))
                    listFiltered.add(restaurant);

            restaurantAdapter = new RestaurantRecyclerViewAdapter(activity, listFiltered);
            recyclerView.setAdapter(restaurantAdapter);
            restaurantAdapter.notifyDataSetChanged();

        });
    }

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