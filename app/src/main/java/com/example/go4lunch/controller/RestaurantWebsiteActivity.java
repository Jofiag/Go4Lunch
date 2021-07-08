package com.example.go4lunch.controller;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.go4lunch.R;
import com.example.go4lunch.data.RestaurantSelectedApi;

public class RestaurantWebsiteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_website);

        /*WebView myWebview = findViewById(R.id.my_webview);
        WebSettings webSettings = myWebview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        String restaurantWebsiteUrl = String.valueOf(RestaurantSelectedApi.getInstance().getRestaurantSelected().getWebsiteUrl());
        myWebview.loadUrl(restaurantWebsiteUrl);*/

        WebView myWebview = new WebView(this);
        setContentView(myWebview);

        String restaurantWebsiteUrl = String.valueOf(RestaurantSelectedApi.getInstance().getRestaurantSelected().getWebsiteUrl());
        myWebview.loadUrl(restaurantWebsiteUrl);
    }
}