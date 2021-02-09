package com.example.go4lunch.controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.go4lunch.R;
import com.example.go4lunch.adapter.RestaurantRecyclerViewAdapter;
import com.example.go4lunch.adapter.WorkmateRecyclerViewAdapter;
import com.example.go4lunch.model.Restaurant;
import com.example.go4lunch.model.Workmate;
import com.example.go4lunch.util.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class RestaurantDetailsActivity extends AppCompatActivity {
    private ImageView yellowStar;
    private ImageView callImageView;
    private ImageView starImageView;
    private ImageView globeImageView;
    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private ImageView restaurantImageView;
    private TextView restaurantNameTextView;
    private TextView foodCountryAndRestaurantAddress;

    private Restaurant restaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);

        setReferences();
        restaurant = getRestaurantSelected();
        setRecyclerView();

    }

    private void setReferences() {
        fab = findViewById(R.id.fab);
        yellowStar = findViewById(R.id.yellow_star);
        globeImageView = findViewById(R.id.globe_image_view);
        starImageView = findViewById(R.id.green_star_image_view);
        callImageView = findViewById(R.id.green_call_image_view);
        recyclerView = findViewById(R.id.restaurant_details_recycler_view);
        restaurantImageView = findViewById(R.id.restaurant_image_view_details);
        restaurantNameTextView = findViewById(R.id.restaurant_name_text_view_details);
        foodCountryAndRestaurantAddress = findViewById(R.id.food_country_and_restaurant_address_details);
    }

    private Restaurant getRestaurantSelected(){
        Restaurant restaurantSelected = new Restaurant();
        Workmate workmate;

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            if (bundle.get(Constants.RESTAURANT_SELECTED_CODE) != null)
                restaurantSelected = (Restaurant) bundle.get(Constants.RESTAURANT_SELECTED_CODE);

            if (bundle.get(Constants.WORKMATE_SELECTED_CODE) != null) {
                workmate = (Workmate) bundle.get(Constants.WORKMATE_SELECTED_CODE);
                restaurantSelected = workmate.getRestaurantChosen();
            }
        }

        return restaurantSelected;
    }

    private void setRecyclerView(){
        WorkmateRecyclerViewAdapter workmateAdapter;
        if (restaurant != null)
            workmateAdapter = new WorkmateRecyclerViewAdapter(restaurant.getWorkmateList());
        else
            workmateAdapter = new WorkmateRecyclerViewAdapter(new ArrayList<>());

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(RestaurantDetailsActivity.this));
        recyclerView.setAdapter(workmateAdapter);
    }


}