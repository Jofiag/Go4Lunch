package com.example.go4lunch.controller;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.go4lunch.R;
import com.example.go4lunch.adapter.WorkmateRecyclerViewAdapter;
import com.example.go4lunch.data.LocationApi;
import com.example.go4lunch.data.RestaurantListUrlApi;
import com.example.go4lunch.data.RestaurantNearbyBank;
import com.example.go4lunch.data.RestaurantSelectedApi;
import com.example.go4lunch.fragment.RestaurantListViewFragment;
import com.example.go4lunch.fragment.RestaurantMapViewFragment;
import com.example.go4lunch.fragment.WorkmateListViewFragment;
import com.example.go4lunch.model.Restaurant;
import com.example.go4lunch.model.User;
import com.example.go4lunch.model.Workmate;
import com.example.go4lunch.util.Constants;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import static com.example.go4lunch.util.Constants.FINE_LOCATION;

public class HomepageActivity extends AppCompatActivity
        implements WorkmateRecyclerViewAdapter.OnWorkmateClickListener, RestaurantNearbyBank.OnMarkerClicked {

    private LocationApi locationApi;
    private RestaurantListUrlApi urlApi;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location deviceLocation;
    private LatLng devicePosition;
    private String url;

    private Toolbar myToolbar;
    private DrawerLayout myDrawerLayout;
    private NavigationView myNavigationView;
    private BottomNavigationView bottomNavigationView;

    private final FragmentManager fragmentManager = getSupportFragmentManager();

    private final Fragment restaurantMapViewFragment = new RestaurantMapViewFragment();
    private final Fragment restaurantListViewFragment = new RestaurantListViewFragment();
    private final Fragment workmateListViewFragment = new WorkmateListViewFragment();
    private Fragment fragmentToShow;
    private Fragment activeFragment;

    private Restaurant restaurantChosen;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_homepage);
        setReferences();

        locationApi = LocationApi.getInstance(this);
        urlApi = RestaurantListUrlApi.getInstance(this);
        setLocationManagerAndListener();
        requestLocationIfPermissionIsGranted();
        url = urlApi.getUrlThroughDeviceLocation();


        user = new User(); //TODO : get user connected from firebase

        restaurantChosen = user.getRestaurantChosen();

        addFragments();

        setBottomNavigationView();

        setMyToolbarAsAppBar();

        setMyDrawerLayout();

        setMyNavigationView();
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> {
                if (result)
                    requestLocationIfPermissionIsGranted();
                else
                    requestPermissionWithinDialog();
            }
    );

    private void setLocationManagerAndListener(){
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
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

    private void requestLocationIfPermissionIsGranted() {
        if (checkSelfPermission(FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 600000, 0, locationListener);
            deviceLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            locationApi.setLocation(deviceLocation);

        }
        else{
            if (shouldShowRequestPermissionRationale(FINE_LOCATION))
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();

            requestPermissionLauncher.launch(FINE_LOCATION);
        }

    }
    private void requestPermissionWithinDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Location permission disable")
                .setMessage("You denied the location permission. It is required to show your location. Do you want to grant the permission")
                .setPositiveButton("YES", (dialog, which) -> requestLocationIfPermissionIsGranted())
                .setNegativeButton("NO", null)
                .create()
                .show();
    }

    private void setReferences() {
        myToolbar = findViewById(R.id.my_toolbar);
        myDrawerLayout = findViewById(R.id.my_drawer);
        myNavigationView = findViewById(R.id.my_navigation_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
    }

    private void addFragments(){
        activeFragment = restaurantMapViewFragment;

        fragmentManager.beginTransaction()
                .add(R.id.fragment_container_homepage_activity, workmateListViewFragment)
                .hide(workmateListViewFragment)
                .commit();

        fragmentManager.beginTransaction()
                .add(R.id.fragment_container_homepage_activity, restaurantListViewFragment)
                .hide(restaurantListViewFragment)
                .commit();

        fragmentManager.beginTransaction()
                .add(R.id.fragment_container_homepage_activity, activeFragment)
                .commit();
    }

    private void showFragment(){
        fragmentManager.beginTransaction().hide(activeFragment).show(fragmentToShow).commit();
        activeFragment = fragmentToShow;
    }

    private void setBottomNavigationView(){
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.restaurant_map_view_item){
                myToolbar.setTitle(Constants.IM_HUNGRY_TITLE_TEXT);
                fragmentToShow = restaurantMapViewFragment;
            }
            else if (id == R.id.restaurant_list_view_item){
                myToolbar.setTitle(Constants.IM_HUNGRY_TITLE_TEXT);
                fragmentToShow = restaurantListViewFragment;
            }
            else if (id == R.id.workmate_list_view_item){
                myToolbar.setTitle(Constants.AVAILABLE_WORKMATES_TITLE_TEXT);
                fragmentToShow = workmateListViewFragment;
            }

            showFragment();

            return true;
        });
    }

    private void setMyToolbarAsAppBar(){
        setSupportActionBar(myToolbar);
    }

    private void setMyDrawerLayout(){
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(HomepageActivity.this,
                myDrawerLayout,
                myToolbar,
                R.string.open_navigation_drawer_description_text, R.string.close_navigation_drawer_description_text);

        myDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setMyNavigationView(){
        myNavigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.your_lunch_item) {
                //Attach fragment corresponding

                if (restaurantChosen != null){
                    RestaurantSelectedApi.getInstance().setRestaurantSelected(restaurantChosen);
                    myDrawerLayout.closeDrawers(); // OR myDrawerLayout.closeDrawer(GravityCompat.START);
                    startActivity(new Intent(HomepageActivity.this, RestaurantDetailsActivity.class));
                }
                else
                    Toast.makeText(this, "You don't chose any restaurant yet!", Toast.LENGTH_SHORT).show();

                return true;
            }
            else if (id == R.id.settings_item) {
                //Attach fragment corresponding
                myDrawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            else if (id == R.id.logout_item) {
                //Attach fragment corresponding
                //TODO : LOGOUT USER FROM FIREBASE AND GO BACK TO MAINACTIVITY
                myDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }


            return true;
        });
    }


    @Override
    //Make sure we close the DrawerLayout when the user click on back button
    public void onBackPressed() {
        if (myDrawerLayout.isEnabled())
            myDrawerLayout.closeDrawers();
        else
            super.onBackPressed();
    }

    private void startRestaurantDetailsActivity(String code, Parcelable parcelable){
        Intent intent = new Intent(HomepageActivity.this, RestaurantDetailsActivity.class);
        intent.putExtra(code, parcelable);
        startActivity(intent);
    }


    @Override
    public void onWorkmateSelected(Workmate workmate) {
        if (workmate.getRestaurantChosen() != null)
            startRestaurantDetailsActivity(Constants.WORKMATE_SELECTED_CODE, workmate);
        else
            Toast.makeText(this, Constants.NO_RESTAURANT_TO_SHOW_TEXT, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkerClickedGetRestaurant(Restaurant restaurant) {
        startRestaurantDetailsActivity(Constants.RESTAURANT_ON_MARKER_CODE, restaurant);
    }
}