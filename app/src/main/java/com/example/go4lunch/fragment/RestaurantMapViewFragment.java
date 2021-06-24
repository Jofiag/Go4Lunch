package com.example.go4lunch.fragment;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
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
import android.widget.TextView;
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
import com.example.go4lunch.data.LocationApi;
import com.example.go4lunch.data.RestaurantListUrlApi;
import com.example.go4lunch.data.RestaurantNearbyBank;
import com.example.go4lunch.model.Restaurant;
import com.example.go4lunch.util.Constants;
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

import java.util.Objects;

import static android.content.Context.LOCATION_SERVICE;
import static com.example.go4lunch.util.Constants.ADDRESS;
import static com.example.go4lunch.util.Constants.FINE_LOCATION;
import static com.example.go4lunch.util.Constants.NAME;

@RequiresApi(api = Build.VERSION_CODES.M)
public class RestaurantMapViewFragment extends Fragment {
    private final OnMapReadyCallback callback;

    private LocationApi locationApi;
    private RestaurantListUrlApi urlApi;

    private ImageButton locationButton;
    private TextView showAllTextView;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location deviceLocation;
    private LatLng devicePosition;

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

        locationApi = LocationApi.getInstance(getContext());
        urlApi = RestaurantListUrlApi.getInstance(getContext());

        if (savedInstanceState != null)
            url = savedInstanceState.getString(Constants.URL_KEY);

        Log.d("ORDER", "onCreate: ");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_restaurant_map_view, container, false);
        locationButton = view.findViewById(R.id.my_location_button);
        showAllTextView = view.findViewById(R.id.show_all_text_view);

        Log.d("ORDER", "onCreateView: ");

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("ORDER", "onViewCreated: ");

        setLocationManagerAndListener();
        requestLocationIfPermissionIsGranted(null);
        initializeSearchViewNeeded();
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
                ZoomOnRestaurantSearched(mGoogleMap, query);
                showAllRestaurantNearby(mGoogleMap);
                addMarkerOnPosition(mGoogleMap, devicePosition, "My position : " + locationApi.getStreetAddressFromPositions(), BitmapDescriptorFactory.HUE_RED);
                return true;    //return true so that the fragment won't be restart
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
        outState.putString("url", url);
    }

    private void setGoogleMap(GoogleMap googleMap) {
        Log.d("ORDER", "setGoogleMap: ");

        mGoogleMap = googleMap;
        requestLocationIfPermissionIsGranted(googleMap);

        if (deviceLocation != null) {
            locationApi.setLocation(deviceLocation);
            devicePosition = locationApi.getPositionFromLocation();
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            locationButton.setOnClickListener(v -> googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(devicePosition, 12)));
            showAllTextView.setOnClickListener(v -> showAllRestaurantNearby(googleMap));
            addMarkerOnPosition(googleMap, devicePosition, "My position : " + locationApi.getStreetAddressFromPositions(), BitmapDescriptorFactory.HUE_RED);
            url = urlApi.getUrlThroughDeviceLocation();
            showAllRestaurantNearby(googleMap);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(devicePosition, 11));
        }
        else
            Toast.makeText(getContext(), "Location not available !", Toast.LENGTH_SHORT).show();

    }

    private void setMapFragment(){
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null)
            mapFragment.getMapAsync(callback);
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

            }

            result = new String(resultArray).trim();

        }

        return result;
    }

    private void addMarkerOnPosition(GoogleMap googleMap, LatLng position, String title, float color){
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(color)));

        if (position == devicePosition)
            marker.setTag(-1);          //when the tag equals -1 we know its the device location, so that we won't start the RestaurantDetailsActivity when the user click on his position.
    }

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
                locationApi.setLocation(location);
                devicePosition = locationApi.getPositionFromLocation();
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
            locationApi.setLocation(deviceLocation);

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

    private void showAllRestaurantNearby(GoogleMap googleMap){
        RestaurantNearbyBank.getInstance(getContext(), googleMap).getRestaurantNearbyList(url, restaurantList -> {

        });
    }

    private void ZoomOnRestaurantSearched(GoogleMap googleMap, String query){
        if (googleMap != null){
            googleMap.clear();                  // Removing all marker added
//            url = getUrl(deviceLocation);      // Getting url to get information about nearby restaurant on google maps.
            url = urlApi.getUrlThroughDeviceLocation();

            RestaurantNearbyBank.getInstance(getContext(), googleMap).getRestaurantNearbyList(url, restaurantList -> {

                for (Restaurant restaurant : restaurantList) {
                    LatLng restaurantPosition = restaurant.getPosition();
                    //Zooming on the restaurant clicked
                    if (restaurantPosition != null && Objects.equals(restaurant.getAddress(), getFromQuery(query, ADDRESS))) {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(restaurantPosition, 20));
                        addMarkerOnPosition(googleMap, restaurantPosition, restaurant.getName(), BitmapDescriptorFactory.HUE_ORANGE);
                    }

                    getFromQuery(query, NAME);
                }

            });
        }
    }

    private void showSuggestions(String query){
        RestaurantNearbyBank.getInstance(getContext(), mGoogleMap).getRestaurantNearbyList(url, restaurantList -> {
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
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
}