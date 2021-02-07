package com.example.go4lunch.model;

import android.net.Uri;
import android.widget.ImageView;

public class Restaurant {
    private String name;
    private String address;
    private Uri imageUri;
    private String foodCountry;
    private OpeningHours openingHours;
    private String howFarFromWorkmate;
    private int numberOfFavorableOpinion;
    private int numberOfInterestedWorkmate;

    public Restaurant() {
    }

    public Restaurant(String name, String address, Uri imageUri, String foodCountry, OpeningHours openingHours, String howFarFromWorkmate, int numberOfFavorableOpinion, int numberOfInterestedWorkmate) {
        this.name = name;
        this.imageUri = imageUri;
        this.address = address;
        this.foodCountry = foodCountry;
        this.openingHours = openingHours;
        this.howFarFromWorkmate = howFarFromWorkmate;
        this.numberOfFavorableOpinion = numberOfFavorableOpinion;
        this.numberOfInterestedWorkmate = numberOfInterestedWorkmate;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public String getFoodCountry() {
        return foodCountry;
    }

    public void setFoodCountry(String foodCountry) {
        this.foodCountry = foodCountry;
    }

    public OpeningHours getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(OpeningHours openingHours) {
        this.openingHours = openingHours;
    }

    public String getHowFarFromWorkmate() {
        return howFarFromWorkmate;
    }

    public void setHowFarFromWorkmate(String howFarFromWorkmate) {
        this.howFarFromWorkmate = howFarFromWorkmate;
    }

    public int getNumberOfFavorableOpinion() {
        return numberOfFavorableOpinion;
    }

    public void setNumberOfFavorableOpinion(int numberOfFavorableOpinion) {
        this.numberOfFavorableOpinion = numberOfFavorableOpinion;
    }

    public int getNumberOfInterestedWorkmate() {
        return numberOfInterestedWorkmate;
    }

    public void setNumberOfInterestedWorkmate(int numberOfInterestedWorkmate) {
        this.numberOfInterestedWorkmate = numberOfInterestedWorkmate;
    }
}
