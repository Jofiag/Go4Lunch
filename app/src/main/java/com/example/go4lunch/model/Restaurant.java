package com.example.go4lunch.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.net.URL;
import java.util.List;

public class Restaurant implements Parcelable {
    private String name;
    private String address;
    private Uri imageUri;
    private String foodCountry;
    private OpeningHours openingHours;
    private String howFarFromWorkmate;
    private int numberOfFavorableOpinion;
    private int numberOfInterestedWorkmate;
    private List<Workmate> workmateList;
    private String phoneNumber;
    private URL websiteUrl;
    private LatLng position;

    public Restaurant() {
    }

    public Restaurant(String name, String address, Uri imageUri, String foodCountry, OpeningHours openingHours, String howFarFromWorkmate, int numberOfFavorableOpinion, int numberOfInterestedWorkmate, List<Workmate> workmateList, String phoneNumber, URL websiteUrl) {
        this.name = name;
        this.address = address;
        this.imageUri = imageUri;
        this.websiteUrl = websiteUrl;
        this.phoneNumber = phoneNumber;
        this.foodCountry = foodCountry;
        this.openingHours = openingHours;
        this.workmateList = workmateList;
        this.howFarFromWorkmate = howFarFromWorkmate;
        this.numberOfFavorableOpinion = numberOfFavorableOpinion;
        this.numberOfInterestedWorkmate = numberOfInterestedWorkmate;

    }

    protected Restaurant(Parcel in) {
        name = in.readString();
        address = in.readString();
        imageUri = in.readParcelable(Uri.class.getClassLoader());
        foodCountry = in.readString();
        howFarFromWorkmate = in.readString();
        numberOfFavorableOpinion = in.readInt();
        numberOfInterestedWorkmate = in.readInt();
        phoneNumber = in.readString();
        position = in.readParcelable(LatLng.class.getClassLoader());
    }

    public static final Creator<Restaurant> CREATOR = new Creator<Restaurant>() {
        @Override
        public Restaurant createFromParcel(Parcel in) {
            return new Restaurant(in);
        }

        @Override
        public Restaurant[] newArray(int size) {
            return new Restaurant[size];
        }
    };

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
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

    public List<Workmate> getWorkmateList() {
        return workmateList;
    }

    public void setWorkmateList(List<Workmate> workmateList) {
        this.workmateList = workmateList;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public URL getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(URL websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(address);
        dest.writeParcelable(imageUri, flags);
        dest.writeString(foodCountry);
        dest.writeString(howFarFromWorkmate);
        dest.writeInt(numberOfFavorableOpinion);
        dest.writeInt(numberOfInterestedWorkmate);
        dest.writeString(phoneNumber);
        dest.writeParcelable(position, flags);
    }
}
