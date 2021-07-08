package com.example.go4lunch.controller;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.go4lunch.R;
import com.example.go4lunch.adapter.WorkmateRecyclerViewAdapter;
import com.example.go4lunch.data.RestaurantNearbyBank;
import com.example.go4lunch.fragment.RestaurantListViewFragment;
import com.example.go4lunch.fragment.RestaurantMapViewFragment;
import com.example.go4lunch.fragment.WorkmateListViewFragment;
import com.example.go4lunch.model.Restaurant;
import com.example.go4lunch.model.Workmate;
import com.example.go4lunch.util.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class HomepageActivity extends AppCompatActivity
        implements WorkmateRecyclerViewAdapter.OnWorkmateClickListener, RestaurantNearbyBank.OnMarkerClicked {

    private Toolbar myToolbar;
    private DrawerLayout myDrawerLayout;
    private NavigationView myNavigationView;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        setReferences();

        attachNewFragment(new RestaurantMapViewFragment());// Showing the RestaurantMapViewFragment when first start the activity

        setBottomNavigationView();

        setMyToolbarAsAppBar();

        setMyDrawerLayout();

        setMyNavigationView();
    }

    private void setReferences() {
        myToolbar = findViewById(R.id.my_toolbar);
        myDrawerLayout = findViewById(R.id.my_drawer);
        myNavigationView = findViewById(R.id.my_navigation_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
    }

    @SuppressLint("NonConstantResourceId")
    private void setBottomNavigationView(){
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            switch (id){
                case R.id.restaurant_map_view_item:
                    myToolbar.setTitle(Constants.IM_HUNGRY_TITLE_TEXT);
                    attachNewFragment(new RestaurantMapViewFragment());
                    return true;
                case R.id.restaurant_list_view_item:
                    myToolbar.setTitle(Constants.IM_HUNGRY_TITLE_TEXT);
                    attachNewFragment(new RestaurantListViewFragment());
                    return true;
                case R.id.workmate_list_view_item:
                    myToolbar.setTitle(Constants.AVAILABLE_WORKMATES_TITLE_TEXT);
                    attachNewFragment(new WorkmateListViewFragment());
                    return true;
            }

            return false;
        });
    }

    private void attachNewFragment(Fragment newFragment){
        /*mapViewFrameLayout.setVisibility(View.GONE);
        otherFrameLayout.setVisibility(View.VISIBLE);*/

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.other_fragment_container_homepage_activity, newFragment)
                .commit();
    }

    /*private void getMapViewFragmentIfExist(){
        otherFrameLayout.setVisibility(View.GONE);
        mapViewFrameLayout.setVisibility(View.VISIBLE);

        FragmentManager fragmentManager =  getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Here we are getting the fragment from the manager, but if it's not created before it's going to be null
        Fragment mapViewFragment = fragmentManager.findFragmentById(R.id.map_view_container_homepage_activity);

        // If the fragment doesn't exist, then we create a new one
        if (mapViewFragment == null){
            mapViewFragment = new RestaurantMapViewFragment();
            fragmentTransaction.replace(R.id.map_view_container_homepage_activity, mapViewFragment)
                    .commit();
        }

    }*/

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
                myDrawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(HomepageActivity.this, RestaurantDetailsActivity.class));
                return true;
            }
            else if (id == R.id.settings_item) {
                //Attach fragment corresponding
                myDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
            else if (id == R.id.logout_item) {
                //Attach fragment corresponding
                myDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            myDrawerLayout.closeDrawers(); // OR myDrawerLayout.closeDrawer(GravityCompat.START);

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