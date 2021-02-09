package com.example.go4lunch.util;

import com.example.go4lunch.model.Restaurant;
import com.example.go4lunch.model.Workmate;

import java.util.ArrayList;
import java.util.List;

public class Constants {
    public static final String H_TEXT = "h";
    public static final String OPEN_UNTIL_TEXT = "open until ";
    public static final String IM_HUNGRY_TITLE_TEXT = "I'm hungry!";
    public static final String CLOSE_AND_OPEN_AT_TEXT = "Closed. Open at ";
    public static final String WORKMATE_SELECTED_CODE = "workmate selected";
    public static final String RESTAURANT_SELECTED_CODE = "restaurant selected";
    public static final String AVAILABLE_WORKMATES_TITLE_TEXT = "Available workmates";

    public static List<Restaurant> getRestaurantList(){
        List<Restaurant> restaurantList = new ArrayList<>();
        restaurantList.add(createCRestaurant("Safari"));
        restaurantList.add(createCRestaurant("Flunch"));
        restaurantList.add(createCRestaurant("La mama"));
        restaurantList.add(createCRestaurant("O'tacos"));
        restaurantList.add(createCRestaurant("Oc pizza"));
        restaurantList.add(createCRestaurant("O'tantik"));
        restaurantList.add(createCRestaurant("McDonald"));
        restaurantList.add(createCRestaurant("Panorama"));
        restaurantList.add(createCRestaurant("Maman africa"));
        restaurantList.add(createCRestaurant("Original tacos"));

        return restaurantList;
    }

    private static Restaurant createCRestaurant(String name){
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);

        return restaurant;
    }

    public static List<Workmate> getWorkmateList(){
        List<Workmate> workmateList = new ArrayList<>();
        workmateList.add(createWorkmate("Angela"));
        workmateList.add(createWorkmate("Bilikiss"));
        workmateList.add(createWorkmate("Carole"));
        workmateList.add(createWorkmate("Dorianne"));
        workmateList.add(createWorkmate("Elizabet"));
        workmateList.add(createWorkmate("Florian"));
        workmateList.add(createWorkmate("Gisele"));
        workmateList.add(createWorkmate("Hodette"));
        workmateList.add(createWorkmate("Imen"));
        workmateList.add(createWorkmate("Jocelyn"));

        return workmateList;
    }

    private static Workmate createWorkmate(String name){
        Workmate workmate = new Workmate();
        workmate.setName(name);

        return workmate;
    }
}
