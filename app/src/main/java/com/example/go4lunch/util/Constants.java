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
        restaurantList.add(createCRestaurant("Safari", "French", "1 Rue Faubourg"));
        restaurantList.add(createCRestaurant("Flunch", "Italian", "67 Rue Vincent"));
        restaurantList.add(createCRestaurant("La mama", "French", "13 Ter Richard Mille "));
        restaurantList.add(createCRestaurant("O'tacos", "French", "51 Avenue de la Liberation"));
        restaurantList.add(createCRestaurant("Oc pizza", "French", "83 Rue Strasbourg"));
        restaurantList.add(createCRestaurant("O'tantik", "French", "21 Boulevard François Mitterand"));
        restaurantList.add(createCRestaurant("McDonald", "American", "43 Rue Jean Jacques Jores"));
        restaurantList.add(createCRestaurant("Panorama", "French", "31 Rue des filletes"));
        restaurantList.add(createCRestaurant("Maman africa", "French", "71 Avenue des Paulines"));
        restaurantList.add(createCRestaurant("Original tacos", "French", "97 Boulevard Resgistre"));

        return restaurantList;
    }

    private static Restaurant createCRestaurant(String name, String foodCountry, String address){
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setFoodCountry(foodCountry);
        restaurant.setAddress(address);

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
