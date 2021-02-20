package com.example.go4lunch.util;

import android.content.SearchRecentSuggestionsProvider;

public class RestaurantSuggestions extends SearchRecentSuggestionsProvider {
    public static final String AUTHORITY = "com.example.go4lunch.util.RestaurantSuggestions";
    public static final int MODE = DATABASE_MODE_QUERIES;

    public RestaurantSuggestions() {
        setupSuggestions(AUTHORITY, MODE);
    }

}
