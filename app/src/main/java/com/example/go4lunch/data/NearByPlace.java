package com.example.go4lunch.data;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.example.go4lunch.util.Constants.LATITUDE;
import static com.example.go4lunch.util.Constants.LONGITUDE;
import static com.example.go4lunch.util.Constants.PLACE_NAME;
import static com.example.go4lunch.util.Constants.VICINITY;

public class NearByPlace extends AsyncTask<Object, String, String> {
    private String url;
    private String googlePlaceData;
    private GoogleMap googleMap;
    private List<HashMap<String, String>> nearbyPlacesList;

    public NearByPlace(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    @Override
    protected String doInBackground(Object... objects) {
        try {
            Log.d("NEARBY", "doInBackground: getting nearby places in background thread");
            googleMap = (GoogleMap) objects[0];
            url = (String) objects[1];

            DownloadUrl downloadUrl =new DownloadUrl();
            googlePlaceData = downloadUrl.readUrl(url);
        } catch (Exception e) {
            Log.d("Exception", "doInBackground: " + e.getMessage());
        }
        return googlePlaceData;
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d("GooglePlacesReadTask", "onPostExecute Entered");

        DataParser dataParser = new DataParser();
        nearbyPlacesList =  dataParser.parse(result);

        addMarkerOnPlaces(nearbyPlacesList);
        Log.d("GooglePlacesReadTask", "onPostExecute Exit");
    }

    private void addMarkerOnPlaces(List<HashMap<String, String>> nearbyPlacesList) {

        for (int i = 0; i < nearbyPlacesList.size(); i++) {
            HashMap<String, String> googlePlace = nearbyPlacesList.get(i);

            double lat = Double.parseDouble(Objects.requireNonNull(googlePlace.get(LATITUDE)));
            double lng = Double.parseDouble(Objects.requireNonNull(googlePlace.get(LONGITUDE)));

            LatLng placePosition = new LatLng(lat, lng);
            String vicinity = googlePlace.get(VICINITY);
            String placeName = googlePlace.get(PLACE_NAME);

            googleMap.addMarker(new MarkerOptions()
                    .position(placePosition)
                    .title(placeName + " : " + vicinity)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placePosition, 13));
        }
    }
}
