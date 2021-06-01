package com.example.go4lunch.data;

import android.util.Log;

import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RestaurantBank {
    public interface ListAsyncResponse {
        void processFinished(List<Place> restaurantList);
    }

    private static RestaurantBank instance;

    public static RestaurantBank getInstance() {
        if (instance == null)
            instance = new RestaurantBank();

        return instance;
    }

    public void getRestaurantList(PlacesClient placesClient,
                                  FindAutocompletePredictionsRequest predictionRequest,
                                  List<Place.Field> placeFields,
                                  final ListAsyncResponse callback){

        placesClient.findAutocompletePredictions(predictionRequest)
                .addOnSuccessListener(response -> {
                    List<Place> restaurantList = new ArrayList<>();
                    List<AutocompletePrediction> predictionList = response.getAutocompletePredictions();

                    int i = 0;
                    for (AutocompletePrediction prediction : predictionList) {
                        FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(prediction.getPlaceId(), placeFields).build();

                        int finalI = i;
                        placesClient.fetchPlace(placeRequest).addOnSuccessListener(fetchPlaceResponse -> {
                            Place place = fetchPlaceResponse.getPlace();

                            if (Objects.requireNonNull(place.getTypes()).contains(Place.Type.RESTAURANT))
                                restaurantList.add(place);

                            if (finalI == predictionList.size() - 1)
                                callback.processFinished(restaurantList);
                        });

                        i++;
                    }
                })
                .addOnFailureListener(e -> Log.d("PLACEFAIL", "onFailure: " + e.getMessage()));
    }
}
