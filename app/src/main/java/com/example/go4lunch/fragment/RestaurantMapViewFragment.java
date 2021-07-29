
package com.example.go4lunch.fragment;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;

import com.example.go4lunch.R;
import com.example.go4lunch.controller.RestaurantDetailsActivity;
import com.example.go4lunch.data.LocationApi;
import com.example.go4lunch.data.RestaurantListManager;
import com.example.go4lunch.data.RestaurantListUrlApi;
import com.example.go4lunch.data.RestaurantNearbyBank2;
import com.example.go4lunch.data.RestaurantSelectedApi;
import com.example.go4lunch.model.Restaurant;
import com.example.go4lunch.util.Constants;
import com.example.go4lunch.util.LoadingDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Objects;

import static com.example.go4lunch.util.Constants.ADDRESS;
import static com.example.go4lunch.util.Constants.NAME;

public class RestaurantMapViewFragment extends Fragment {

    private String url;
    private GoogleMap mGoogleMap;
    private LatLng devicePosition;
    private CursorAdapter adapter;
    private String[] columnPlaces;
    private SearchView searchView;
    private ImageButton locationButton;
    private RestaurantListManager listManager;
    private final OnMapReadyCallback callback;

    public RestaurantMapViewFragment() {
        callback = this::setGoogleMap;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ORDER", "onCreate: ");

        setDevicePositionAndListUrl();
        showRestaurantsAndSetOnMarkerClickListener();
        getUrlSaved(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
        listManager.registerBroadcastReceiverFromManager(Constants.SEND_LIST_ACTION);
    }

    @Override
    public void onStop() {
        super.onStop();
        listManager.unregisterBroadcastReceiverFromManager();
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

        locationButton = view.findViewById(R.id.my_location_button);
        initializeSearchViewNeeded();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_view_menu, menu);
        setOurSearchView(menu);
        Log.d("ORDER", "onCreateOptionsMenu: ");
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
        outState.putString(Constants.URL_KEY, url);
    }


    private void setMapFragment(){
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null)
            mapFragment.getMapAsync(callback);
    }
    private void setGoogleMap(GoogleMap googleMap) {
        Log.d("ORDER", "setGoogleMap: ");
        mGoogleMap = googleMap;
        /*if (deviceLocation != null) {
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
            locationButton.setOnClickListener(v -> mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(devicePosition, 12)));

            addMarkerOnPosition(devicePosition,
                    "My position : " + LocationApi.getInstance(getContext()).getStreetAddressFromPositions(),
                    BitmapDescriptorFactory.HUE_RED);
            addMarkerToAllRestaurants();

            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(devicePosition, 14));
        }
        else
            Toast.makeText(getContext(), "Location not available !", Toast.LENGTH_SHORT).show();*/
    }

    private void setDevicePositionAndListUrl(){
        devicePosition = LocationApi.getInstance(getContext()).getPositionFromLocation();
        url = RestaurantListUrlApi.getInstance(getContext()).getUrlThroughDeviceLocation();
    }

    private void showRestaurantsAndSetOnMarkerClickListener(){
        listManager = RestaurantListManager.getInstance(requireContext());

        listManager.receiveRestaurantList(restaurantList -> {
            if (mGoogleMap != null){
                addMarkerOnAllRestaurantsAndDevicePosition(restaurantList);
                startRestaurantDetailsActivityWhenMarkerIsClicked(restaurantList);
            }
        });
    }
    private void addMarkerOnAllRestaurantsAndDevicePosition(@org.jetbrains.annotations.NotNull ArrayList<Restaurant> restaurantList){
        mGoogleMap.clear();

        for (Restaurant restaurant : restaurantList)
            addMarkerOnPosition(restaurant.getPosition(), restaurant.getName(), restaurant.getAddress(), BitmapDescriptorFactory.HUE_ORANGE);

        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        addMarkerOnPosition(devicePosition, LocationApi.getInstance(requireContext()).getStreetAddressFromPositions(), Constants.DEVICE_POSITION, BitmapDescriptorFactory.HUE_RED);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(devicePosition, 14));

        locationButton.setOnClickListener(v -> mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(devicePosition, 15)));
    }
    private void startRestaurantDetailsActivityWhenMarkerIsClicked(@org.jetbrains.annotations.NotNull ArrayList<Restaurant> restaurantList){
        mGoogleMap.setOnMarkerClickListener(marker -> {
            for (Restaurant restaurant : restaurantList) {
                if (restaurant.getAddress().equals(marker.getTag())){
                    RestaurantSelectedApi.getInstance().setRestaurantSelected(restaurant);
                    startActivity(new Intent(requireActivity(), RestaurantDetailsActivity.class));
                }
            }

            return false;
        });
    }
    private void addMarkerOnPosition(LatLng position, String title, String tag, float color){
        Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                .position(position)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(color)));

        if (marker != null)
            marker.setTag(tag);
    }
    private void ZoomOnRestaurantSearched(String query){
        if (mGoogleMap != null){
            url = RestaurantListUrlApi.getInstance(getContext()).getUrlThroughDeviceLocation();

            RestaurantNearbyBank2.getInstance(requireActivity().getApplication()).getRestaurantList(url, restaurantList -> {
                for (Restaurant restaurant : restaurantList) {
                    LatLng restaurantPosition = restaurant.getPosition();
                    //Zooming on the restaurant clicked
                    if (restaurantPosition != null && Objects.equals(restaurant.getAddress(), getFromQuery(query, ADDRESS))) {
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(restaurantPosition, 20));
                        addMarkerOnPosition(restaurantPosition, restaurant.getName(), restaurant.getAddress(), BitmapDescriptorFactory.HUE_ORANGE);
                    }

                    getFromQuery(query, NAME);
                }
            });

            /*RestaurantNearbyBank.getInstance(getActivity(), mGoogleMap).getRestaurantNearbyList(url, restaurantList -> {

            });*/
        }
    }

    private void initializeSearchViewNeeded() {
        columnPlaces = new String[]{
                BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1, //The main line of a suggestion (necessary)
                SearchManager.SUGGEST_COLUMN_TEXT_2  //The second line for a secondary text (optional)
        };

        int[] viewIds = new int[]{
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
    private void setOurSearchView(Menu menu){
        MenuItem searchItem = menu.findItem(R.id.search_item);
        LoadingDialog dialog = LoadingDialog.getInstance(requireActivity());


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

        searchView.setSuggestionsAdapter(adapter);
        searchView.setSubmitButtonEnabled(true);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                dialog.startLoadingDialog();

                ZoomOnRestaurantSearched(query);

                dialog.dismissLoadingDialog();

                return true;    //return true so that the fragment won't be restart
            }

            @Override
            public boolean onQueryTextChange(String newText) {
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

                    if (y != 0 && i > y) {                                      //When we're at the character '_' position,
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

            }

            result = new String(resultArray).trim();

        }

        return result;
    }
    private void showSuggestions(String query){
        RestaurantNearbyBank2.getInstance(requireActivity().getApplication()).getRestaurantList(url, restaurantList -> {
            if (columnPlaces != null && adapter != null && query != null) {
                //When we've got all the restaurant
                if (!restaurantList.isEmpty()) {
                    //Then we add all of them to our cursor to show it as suggestions to the user
                    int y = 0;
                    final MatrixCursor cursor = new MatrixCursor(columnPlaces);

                    for (Restaurant restaurant : restaurantList) {
                        if (restaurant != null && restaurant.getName() != null) {
                            String placeName = restaurant.getName();

                            if (placeName.toLowerCase().contains(query.toLowerCase()))
                                cursor.addRow(new Object[]{y, placeName, restaurant.getAddress()});
                        }

                        y++;
                    }

                    adapter.changeCursor(cursor);
                }

                if (searchView != null)
                    searchView.setSuggestionsAdapter(adapter);
            }
        });

        /* AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        RectangularBounds bounds = RectangularBounds.newInstance(
                addDistanceToPosition(devicePosition, - 1000, - 1000),
                addDistanceToPosition(devicePosition, 1000, 1000)
        );
        String country = requireContext().getResources().getConfiguration().getLocales().get(0).getCountry();//.getCountry();
//        String countryCode = String.valueOf(country.charAt(0) + country.charAt(1)).toUpperCase();
        Log.d("COUNTRY", "showSuggestions: " + country);
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setLocationRestriction(bounds)
                .setOrigin(devicePosition)
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setSessionToken(token)
                .setCountry(country)
                .setQuery(query)
                .build();

//        FetchPlaceRequest request1 = FetchPlaceRequest.

        if(!Places.isInitialized())
            Places.initialize(requireContext(), requireContext().getString(R.string.google_maps_key));

        PlacesClient placesClient = Places.createClient(requireContext());
        placesClient.findAutocompletePredictions(request).addOnSuccessListener(findAutocompletePredictionsResponse -> {
            int y = 0;
            final MatrixCursor cursor = new MatrixCursor(columnPlaces);
            for (AutocompletePrediction prediction : findAutocompletePredictionsResponse.getAutocompletePredictions()) {
                if (prediction.getPlaceTypes().contains(Place.Type.RESTAURANT)) {
                    Log.d("PREDICTION", "onSuccess: Primary : " + prediction.getPrimaryText(null).toString());
                    Log.d("PREDICTION", "onSuccess: Secondary : " + prediction.getSecondaryText(null).toString());
                    if (columnPlaces != null && adapter != null && query != null) {
                        //When we've got all the restaurant
                        //Then we add all of them to our cursor to show it as suggestions to the user


                        String placeName = String.valueOf(prediction.getPrimaryText(null));
                        String placeAddress = String.valueOf(prediction.getSecondaryText(null));

                        if (placeName.toLowerCase().contains(query.toLowerCase()) || query.isEmpty())
                            cursor.addRow(new Object[]{y, placeName, placeAddress});


                        y++;

                        adapter.changeCursor(cursor);

                        if (searchView != null)
                            searchView.setSuggestionsAdapter(adapter);
                    }
                }
            }
        }).addOnFailureListener(e -> {

        });
*/
    }

    private void getUrlSaved(Bundle savedInstanceState){
        if (savedInstanceState != null)
            url = savedInstanceState.getString(Constants.URL_KEY);
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



    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /*private void addMarkerToAllRestaurants(){
        LoadingDialog dialog  = LoadingDialog.getInstance(getActivity());
        dialog.startLoadingDialog();
        RestaurantNearbyBank.getInstance(getActivity(), mGoogleMap).getRestaurantNearbyList(url, restaurantList -> dialog.dismissLoadingDialog());
    }*/

    /*private BitmapDescriptor getBitmapFromVectorAssets(Context context, int id){
            Drawable vectorDrawable = ContextCompat.getDrawable(context, id);
            assert vectorDrawable != null;
            vectorDrawable.setBounds(0,0,vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

            Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.draw(canvas);

            return BitmapDescriptorFactory.fromBitmap(bitmap);
        }

        private String getQuerySearched(){
            Intent intent = requireActivity().getIntent();
            SearchRecentSuggestions searchRecentSuggestions = new SearchRecentSuggestions(getContext(),
                    RestaurantSuggestions.AUTHORITY,
                    RestaurantSuggestions.MODE);

            intent.putExtra("url", url);

            String query = null;

            if (Intent.ACTION_SEARCH.equals(intent.getAction())){
                query = intent.getStringExtra(SearchManager.QUERY);
                searchRecentSuggestions.saveRecentQuery(query, null);

            }

            return query;
        }*/

    /* public static LatLng addDistanceToPosition(LatLng originPosition, long distanceOnLat, long distanceOnLng) {
        double latOrigin = originPosition.latitude;
        double lngOrigin = originPosition.longitude;

        double lat = latOrigin + (180 / Math.PI) * (distanceOnLat / 6378137.0);
        double lng = lngOrigin + (180 / Math.PI) * (distanceOnLng / 6378137.0) / Math.cos(latOrigin);

        return new LatLng(lat, lng);
    }*/

}