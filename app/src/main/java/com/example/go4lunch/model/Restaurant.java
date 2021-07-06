package com.example.go4lunch.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Restaurant implements Parcelable {
    private String name;
    private String address;
    private Uri websiteUrl;
    private String placeId;
    private String imageUrl;
    private LatLng position;
    private String foodCountry;
    private String phoneNumber;
    private int favorableOpinion;
    private List<Workmate> workmateList;
    private MyOpeningHours myOpeningHours;
    private int numberOfInterestedWorkmate;
    private int distanceFromDeviceLocation;


    public Restaurant() {
    }

    protected Restaurant(Parcel in) {
        name = in.readString();
        address = in.readString();
        imageUrl = in.readString();
        phoneNumber = in.readString();
        foodCountry = in.readString();
        favorableOpinion = in.readInt();
        numberOfInterestedWorkmate = in.readInt();
        position = in.readParcelable(LatLng.class.getClassLoader());
        distanceFromDeviceLocation = in.readInt();
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

    public int getDistanceFromDeviceLocation() {
        return distanceFromDeviceLocation;
    }

    public void setDistanceFromDeviceLocation(int distanceFromDeviceLocation) {
        this.distanceFromDeviceLocation = distanceFromDeviceLocation;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFoodCountry() {
        return foodCountry;
    }

    public void setFoodCountry(String foodCountry) {
        this.foodCountry = foodCountry;
    }

    public MyOpeningHours getOpeningHours() {
        return myOpeningHours;
    }

    public void setOpeningHours(MyOpeningHours myOpeningHours) {
        this.myOpeningHours = myOpeningHours;
    }

    public int getFavorableOpinion() {
        return favorableOpinion;
    }

    public void setFavorableOpinion(int favorableOpinion) {
        this.favorableOpinion = favorableOpinion;
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

    public Uri getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(Uri websiteUrl) {
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
        dest.writeString(imageUrl);
        dest.writeString(foodCountry);
        dest.writeInt(favorableOpinion);
        dest.writeInt(numberOfInterestedWorkmate);
        dest.writeString(phoneNumber);
        dest.writeParcelable(position, flags);
        dest.writeInt(distanceFromDeviceLocation);
    }
}
