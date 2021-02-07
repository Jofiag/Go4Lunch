package com.example.go4lunch.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.go4lunch.R;
import com.example.go4lunch.model.Restaurant;
import com.example.go4lunch.model.Workmate;
import com.example.go4lunch.util.Constants;

public class RestaurantDetailsActivity extends AppCompatActivity {
    private Restaurant restaurant;
    private Workmate workmate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            if (bundle.getSerializable(Constants.RESTAURANT_SELECTED_CODE) != null)
                restaurant = (Restaurant) bundle.getSerializable(Constants.RESTAURANT_SELECTED_CODE);

            if (bundle.getSerializable(Constants.WORKMATE_SELECTED_CODE) != null) {
                workmate = (Workmate) bundle.getSerializable(Constants.WORKMATE_SELECTED_CODE);
                restaurant = workmate.getRestaurantChosen();
            }
        }


    }
}