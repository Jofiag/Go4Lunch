package com.example.go4lunch.controller;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.go4lunch.R;
import com.example.go4lunch.adapter.WorkmateRecyclerViewAdapter;
import com.example.go4lunch.data.RestaurantSelectedApi;
import com.example.go4lunch.model.Restaurant;
import com.example.go4lunch.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RestaurantDetailsActivity extends AppCompatActivity {
    public static final String CALL_PERMISSION = Manifest.permission.CALL_PHONE;
    public static final int PERMISSION_GRANTED = PackageManager.PERMISSION_GRANTED;
//    public static final String WEB_PERMISSION = Manifest.permission.;

    private ImageView yellowStar;
    private ImageView callImageView;
    private ImageView starImageView;
    private ImageView globeImageView;
    private CircleImageView chosenImageView;
    private RecyclerView recyclerView;
    private ImageView restaurantImageView;
    private TextView restaurantNameTextView;
    private TextView RestaurantFoodCountryAndRestaurantAddress;

    private List<Restaurant> restaurantLikedList = new ArrayList<>();

    private Restaurant restaurant;
    private User user;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);

        setReferences();

        user = getUserConnected();
        restaurantLikedList = getRestaurantLikedList();
        restaurant = RestaurantSelectedApi.getInstance().getRestaurantSelected();

        showRestaurantImageNameAndAddress();
        setRecyclerView();
        setCallRestaurantFunction();
        setLikeRestaurantFunction();
        setGoToRestaurantWebsiteFunction();
        indicateIfRestaurantIsChosenByWorkmate();
        setYellowStarVisibility();
        setChosenImageViewSource();
        setRestaurantChosenByWorkmateMarkerInGreen();

    }

    private void setRestaurantChosenByWorkmateMarkerInGreen() {
        //TODO : Set marker of restaurant chosen by workmate in green
    }

    private void setReferences() {
        chosenImageView = findViewById(R.id.chosenImageView);
        yellowStar = findViewById(R.id.yellow_star);
        globeImageView = findViewById(R.id.globe_image_view);
        starImageView = findViewById(R.id.green_star_image_view);
        callImageView = findViewById(R.id.green_call_image_view);
        recyclerView = findViewById(R.id.restaurant_details_recycler_view);
        restaurantImageView = findViewById(R.id.restaurant_image_view_details);
        restaurantNameTextView = findViewById(R.id.restaurant_name_text_view_details);
        RestaurantFoodCountryAndRestaurantAddress = findViewById(R.id.food_country_and_restaurant_address_details);
    }

    private User getUserConnected(){


        return new User();
    }

    public List<Restaurant> getRestaurantLikedList() {
        restaurantLikedList = user.getRestaurantLikedList();

        if (restaurantLikedList == null)
            restaurantLikedList = new ArrayList<>();

        return restaurantLikedList;
    }

    private void setYellowStarVisibility(){
        if (restaurantLikedList.contains(restaurant))
            yellowStar.setVisibility(View.VISIBLE);
        else
            yellowStar.setVisibility(View.GONE);
    }

    private void setChosenImageViewSource(){
        if (user.getRestaurantChosen() == restaurant)
            chosenImageView.setImageResource(R.mipmap.green_check_round);
        else
            chosenImageView.setImageResource(R.mipmap.red_unchecked);
    }

    //    private Restaurant getRestaurantSelected(){
//        Restaurant restaurantSelected = new Restaurant();
//        Workmate workmate;
//
//        Bundle bundle = getIntent().getExtras();
//
//        if (bundle != null) {
//            if (bundle.get(Constants.RESTAURANT_SELECTED_CODE) != null)
//                restaurantSelected = (Restaurant) bundle.get(Constants.RESTAURANT_SELECTED_CODE);
//
//            if (bundle.get(Constants.WORKMATE_SELECTED_CODE) != null) {
//                workmate = (Workmate) bundle.get(Constants.WORKMATE_SELECTED_CODE);
//                restaurantSelected = workmate.getRestaurantChosen();
//            }
//
//            if (bundle.get(Constants.RESTAURANT_ON_MARKER_CODE) != null){
//                restaurantSelected = (Restaurant) bundle.get(Constants.RESTAURANT_ON_MARKER_CODE);
//                Toast.makeText(this, "Name = " + restaurantSelected.getName(), Toast.LENGTH_SHORT).show();
//
//            }
//
//        }
//
//        return restaurantSelected;
//    }

    private void setRecyclerView(){
        WorkmateRecyclerViewAdapter workmateAdapter;
        if (restaurant != null)
            workmateAdapter = new WorkmateRecyclerViewAdapter(restaurant.getWorkmateList());
        else
            workmateAdapter = new WorkmateRecyclerViewAdapter(new ArrayList<>());

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(RestaurantDetailsActivity.this));
        recyclerView.setAdapter(workmateAdapter);
    }

    private void showRestaurantImageNameAndAddress(){
        if (restaurant != null) {
            if (restaurant.getImageUrl() != null)
                Picasso.get().load(restaurant.getImageUrl())
                        .placeholder(android.R.drawable.stat_sys_download)
                        .error(android.R.drawable.stat_notify_error)
                        .resize(445, 445)
                        .into(restaurantImageView);

            /*if (restaurant.getFoodCountry() != null || restaurant.getFoodCountry().isEmpty())
                RestaurantFoodCountryAndRestaurantAddress.setText(MessageFormat.format("{0} - {1}", restaurant.getFoodCountry(), restaurant.getAddress()));
            else*/
                RestaurantFoodCountryAndRestaurantAddress.setText(restaurant.getAddress());

            restaurantNameTextView.setText(restaurant.getName());
        }
    }

    private void setLikeRestaurantFunction(){

        starImageView.setOnClickListener(v -> {
            //Add actual restaurant to the liked restaurant list of the workmate connected and set yellowStar visibility to VISIBLE
            int visibility = yellowStar.getVisibility();
            String status = "";
            if (visibility != View.VISIBLE) {
                yellowStar.setVisibility(View.VISIBLE);

                if (!restaurantLikedList.contains(restaurant))
                    restaurantLikedList.add(restaurant);

                status = " added to liked list.";
            }

            if (visibility == View.VISIBLE){
                yellowStar.setVisibility(View.GONE);
                restaurantLikedList.remove(restaurant);
                status = " removed from liked list.";
            }

            Toast.makeText(this, restaurant.getName() + status, Toast.LENGTH_SHORT).show();
            user.setRestaurantLikedList(restaurantLikedList);
            //TODO : update user to firebase
        });
    }

    private void setGoToRestaurantWebsiteFunction(){
        globeImageView.setOnClickListener(v -> {
            //Go to restaurant website if its available.
            if(restaurant.getWebsiteUrl() != null)
                startActivity(new Intent(this, RestaurantWebsiteActivity.class));
            else
                Toast.makeText(this, "Website not available for " + restaurant.getName(), Toast.LENGTH_SHORT).show();
        });
    }

    private void indicateIfRestaurantIsChosenByWorkmate(){
        //If workmate connected has chosen actual restaurant, set fab visibility to VISIBLE
        chosenImageView.setOnClickListener(v -> {
            String status;
            Restaurant restaurantTemp;

            if (user.getRestaurantChosen() == restaurant){
                chosenImageView.setImageResource(R.mipmap.red_unchecked);
                restaurantTemp = null;
                status = " not chosen anymore.";
            }
            else{
                chosenImageView.setImageResource(R.mipmap.green_check_round);
                restaurantTemp = restaurant;
                status = " chosen.";
            }

            Toast.makeText(RestaurantDetailsActivity.this, restaurant.getName() + status, Toast.LENGTH_SHORT).show();
            user.setRestaurantChosen(restaurantTemp);
            //TODO : update user to firebase
        });
    }



//    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
//            new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
//                @RequiresApi(api = Build.VERSION_CODES.M)
//                @Override
//                public void onActivityResult(Boolean result) {
//                    if (result)
//                        callPhoneNumberIfPermissionGranted();
//                    else
//                        askPermissionWithinDialog();
//                }
//            }
//    );
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> {
                if (result)
                    callPhoneNumberIfPermissionGranted();
                else
                    askPermissionWithinDialog();
            }
);
    private void setCallRestaurantFunction(){
        callImageView.setOnClickListener(v -> {
            //Call restaurant if phone number is available
            Toast.makeText(RestaurantDetailsActivity.this, "Call", Toast.LENGTH_SHORT).show();
            callPhoneNumberIfPermissionGranted();
            Log.d("DETAILS", "setCallRestaurantFunction: CALL");
        });
    }

    private void call(){
        String dial = "tel:" + restaurant.getPhoneNumber();
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(dial));
        startActivity(callIntent);
    }

    private void callPhoneNumberIfPermissionGranted(){
        if (checkSelfPermission(CALL_PERMISSION) == PERMISSION_GRANTED){
            call();
        }
        else{
            if (shouldShowRequestPermissionRationale(CALL_PERMISSION))
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();

            requestPermissionLauncher.launch(CALL_PERMISSION);
        }
    }

    private void askPermissionWithinDialog(){
        new AlertDialog.Builder(this)
                .setTitle("Call phone permission disable")
                .setMessage("You denied the Call phone permission. It is required to to call the restaurant. Do you want to grant the permission")
                .setPositiveButton("YES", (dialog, which) -> callPhoneNumberIfPermissionGranted())
                .setNegativeButton("NO", null)
                .create()
                .show();
    }

//    @RequiresApi(api = Build.VERSION_CODES.M)
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_CALL_PERMISSION_CODE){
//            if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED)
//                call(restaurant.getPhoneNumber());
//        }
//        else
//            askPermissionWithinDialog();
//    }
}