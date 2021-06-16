package com.example.go4lunch.data;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.go4lunch.util.Constants.GEOMETRY;
import static com.example.go4lunch.util.Constants.LATITUDE;
import static com.example.go4lunch.util.Constants.LOCATION;
import static com.example.go4lunch.util.Constants.LONGITUDE;
import static com.example.go4lunch.util.Constants.NAME;
import static com.example.go4lunch.util.Constants.PLACE_NAME;
import static com.example.go4lunch.util.Constants.REFERENCE;
import static com.example.go4lunch.util.Constants.RESULTS;
import static com.example.go4lunch.util.Constants.VICINITY;

public class DataParser {

    public List<HashMap<String, String>> parse(String jsonData) {
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try {
            Log.d("Places", "parse");
            jsonObject = new JSONObject((String) jsonData);
            jsonArray = jsonObject.getJSONArray(RESULTS);
        }
        catch (JSONException e) {
            Log.d("Places", "parse error");
            e.printStackTrace();
        }

        assert jsonArray != null;

        return getPlaces(jsonArray);
    }

    private List<HashMap<String, String>> getPlaces(JSONArray jsonArray) {
        int placesCount = jsonArray.length();
        List<HashMap<String, String>> placesList = new ArrayList<>();
        HashMap<String, String> placeMap = null;
        Log.d("Places", "getPlaces");

        for (int i = 0; i < placesCount; i++) {
            try {
                placeMap = getPlace((JSONObject) jsonArray.get(i));
                placesList.add(placeMap);
                Log.d("Places", "Adding places");

            } catch (JSONException e) {
                Log.d("Places", "Error in Adding places");
                e.printStackTrace();
            }
        }

        return placesList;
    }

    private HashMap<String, String> getPlace(JSONObject googlePlaceJson) {
        HashMap<String, String> googlePlaceMap = new HashMap<String, String>();
        String placeName = "-NA-";
        String vicinity = "-NA-";
        String latitude = "";
        String longitude = "";
        String reference = "";

        Log.d("getPlace", "Entered");

        try {

            if (!googlePlaceJson.isNull(NAME))
                placeName = googlePlaceJson.getString(NAME);

            if (!googlePlaceJson.isNull(VICINITY))
                vicinity = googlePlaceJson.getString(VICINITY);

            latitude = googlePlaceJson.getJSONObject(GEOMETRY).getJSONObject(LOCATION).getString(LATITUDE);
            longitude = googlePlaceJson.getJSONObject(GEOMETRY).getJSONObject(LOCATION).getString(LONGITUDE);
            reference = googlePlaceJson.getString(REFERENCE);

            googlePlaceMap.put(PLACE_NAME, placeName);
            googlePlaceMap.put(VICINITY, vicinity);
            googlePlaceMap.put(LATITUDE, latitude);
            googlePlaceMap.put(LONGITUDE, longitude);
            googlePlaceMap.put(REFERENCE, reference);

            Log.d("getPlace", "Putting Places");
        }
        catch (JSONException e) {
            Log.d("Exception", "getPlace: " + e.getMessage());
        }

        return googlePlaceMap;
    }

}
