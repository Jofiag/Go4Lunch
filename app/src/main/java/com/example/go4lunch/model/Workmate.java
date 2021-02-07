package com.example.go4lunch.model;

import android.widget.ImageView;

public class Workmate {
    private String name;
    private ImageView image;
    private Restaurant restaurantChosen;

    public Workmate() {
    }

    public Workmate(String name, ImageView image, Restaurant restaurantChosen) {
        this.name = name;
        this.image = image;
        this.restaurantChosen = restaurantChosen;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ImageView getImage() {
        return image;
    }

    public void setImage(ImageView image) {
        this.image = image;
    }

    public Restaurant getRestaurantChosen() {
        return restaurantChosen;
    }

    public void setRestaurantChosen(Restaurant restaurantChosen) {
        this.restaurantChosen = restaurantChosen;
    }
}
