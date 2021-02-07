package com.example.go4lunch.model;

import android.net.Uri;
import android.widget.ImageView;

import java.io.Serializable;

public class Workmate implements Serializable {
    private String name;
    private Uri imageUri;
    private Restaurant restaurantChosen;

    public Workmate() {
    }

    public Workmate(String name, Uri imageUri, Restaurant restaurantChosen) {
        this.name = name;
        this.imageUri = imageUri;
        this.restaurantChosen = restaurantChosen;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public Restaurant getRestaurantChosen() {
        return restaurantChosen;
    }

    public void setRestaurantChosen(Restaurant restaurantChosen) {
        this.restaurantChosen = restaurantChosen;
    }
}
