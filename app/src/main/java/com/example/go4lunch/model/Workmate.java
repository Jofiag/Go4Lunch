package com.example.go4lunch.model;

import android.net.Uri;

import java.io.Serializable;
import java.util.List;

public class Workmate implements Serializable {
    private String name;
    private Uri imageUri;
    private Restaurant restaurantChosen;
    private List<Restaurant> restaurantLikedList;

    public Workmate() {
    }

    public Workmate(String name, Uri imageUri, Restaurant restaurantChosen, List<Restaurant> restaurantLikedList) {
        this.name = name;
        this.imageUri = imageUri;
        this.restaurantChosen = restaurantChosen;
        this.restaurantLikedList = restaurantLikedList;
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

    public List<Restaurant> getRestaurantLikedList() {
        return restaurantLikedList;
    }

    public void setRestaurantLikedList(List<Restaurant> restaurantLikedList) {
        this.restaurantLikedList = restaurantLikedList;
    }
}
