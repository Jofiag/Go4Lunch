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
    public static final String HAS_NOT_DECIDED_YET = " hasn't decided yet";
    public static final String NO_RESTAURANT_TO_SHOW_TEXT = "No restaurant to show!";
    public static final int AUTOCOMPLETE_REQUEST_CODE = 1;

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
        List<Restaurant> restaurantList = getRestaurantList();

        List<Workmate> workmateList = new ArrayList<>();
        workmateList.add(createWorkmate("Angela", restaurantList.get(0)));
        workmateList.add(createWorkmate("Bilikiss", restaurantList.get(1)));
        workmateList.add(createWorkmate("Carole", restaurantList.get(2)));
        workmateList.add(createWorkmate("Dorianne", restaurantList.get(3)));
        workmateList.add(createWorkmate("Elizabet", restaurantList.get(4)));
        workmateList.add(createWorkmate("Florian", restaurantList.get(5)));
        workmateList.add(createWorkmate("Gisele", restaurantList.get(6)));
        workmateList.add(createWorkmate("Hodette", null));
        workmateList.add(createWorkmate("Imen", null));
        workmateList.add(createWorkmate("Jocelyn", null));

        return workmateList;
    }

    private static Workmate createWorkmate(String name, Restaurant restaurant){
        Workmate workmate = new Workmate();
        workmate.setName(name);
        workmate.setRestaurantChosen(restaurant);
        return workmate;
    }
}
