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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.go4lunch.R;
import com.example.go4lunch.adapter.RestaurantRecyclerViewAdapter;
import com.example.go4lunch.data.RestaurantListUrlApi;
import com.example.go4lunch.data.RestaurantNearbyBank2;
import com.example.go4lunch.model.Restaurant;
import com.example.go4lunch.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class RestaurantListViewFragment extends Fragment{
    private String url;
    private Activity activity;
    private RecyclerView recyclerView;
    private RestaurantRecyclerViewAdapter restaurantAdapter;

    public RestaurantListViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

        activity = getActivity();
        url = RestaurantListUrlApi.getInstance(getActivity()).getUrlThroughDeviceLocation();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_restaurant_list_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.restaurant_list_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        url = RestaurantListUrlApi.getInstance(getActivity()).getUrlThroughDeviceLocation();

        RestaurantNearbyBank2.getInstance(requireActivity().getApplication()).getRestaurantList(url, restaurantList -> {
            if (restaurantList.isEmpty())
                Toast.makeText(activity, "No restaurant found !!!", Toast.LENGTH_SHORT).show();
            restaurantAdapter = new RestaurantRecyclerViewAdapter(activity, removeRedundantRestaurant(restaurantList));
            recyclerView.setAdapter(restaurantAdapter);
            restaurantAdapter.notifyDataSetChanged();
        });

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

        RestaurantNearbyBank2.getInstance(requireActivity().getApplication()).getRestaurantList(url, restaurantList -> {
            List<Restaurant> listFiltered = new ArrayList<>();

            for (Restaurant restaurant : restaurantList)
                if (restaurant.getName().toLowerCase().contains(query.toLowerCase()))
                    listFiltered.add(restaurant);

            restaurantAdapter = new RestaurantRecyclerViewAdapter(activity, listFiltered);
            recyclerView.setAdapter(restaurantAdapter);
            restaurantAdapter.notifyDataSetChanged();

        });

        /*RestaurantNearbyBank.getInstance(activity, null).getRestaurantNearbyList(url, restaurantList -> {
            List<Restaurant> listFiltered = new ArrayList<>();

            for (Restaurant restaurant : restaurantList)
                if (restaurant.getName().toLowerCase().contains(query.toLowerCase()))
                    listFiltered.add(restaurant);

            restaurantAdapter = new RestaurantRecyclerViewAdapter(activity, listFiltered);
            recyclerView.setAdapter(restaurantAdapter);
            restaurantAdapter.notifyDataSetChanged();

        });*/
    }

    private ArrayList<Restaurant> removeRedundantRestaurant(ArrayList<Restaurant> list){
        List<Integer> indexList = new ArrayList<>();


        for (int i = 0; i < list.size(); i++) {
            Restaurant restaurantC = list.get(i);

            for (int y = 0; y < list.size(); y++) {
                Restaurant restaurantO = list.get(i);
                if (i != y){
                    if (restaurantC == restaurantO)
                        indexList.add(y);
                }
            }
        }

        if (!indexList.isEmpty()) {
            for (Integer integer : indexList) {
                if (integer < list.size()){
                    Restaurant restaurantR = list.get(integer);
                    list.remove(restaurantR);
                }
            }
        }

        return list;
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