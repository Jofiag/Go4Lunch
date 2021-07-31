package com.example.go4lunch.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.go4lunch.R;
import com.example.go4lunch.controller.RestaurantDetailsActivity;
import com.example.go4lunch.data.RestaurantListUrlApi;
import com.example.go4lunch.data.RestaurantNearbyBank2;
import com.example.go4lunch.data.RestaurantSelectedApi;
import com.example.go4lunch.model.MyOpeningHours;
import com.example.go4lunch.model.Restaurant;
import com.example.go4lunch.util.Constants;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class RestaurantRecyclerViewAdapter extends RecyclerView.Adapter<RestaurantRecyclerViewAdapter.MyViewHolder>
implements Filterable {
    private final Activity activity;
    private final List<Restaurant> restaurantList;

    private MyViewHolder myViewHolder;
    private Restaurant currentRestaurant;

    public RestaurantRecyclerViewAdapter(Activity activity, List<Restaurant> restaurantList) {
        this.activity = activity;
        this.restaurantList = restaurantList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_row, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        myViewHolder = holder;
        currentRestaurant = restaurantList.get(position);

        if (currentRestaurant != null)
            showRestaurantAttributes(position);
    }

    private void showRestaurantAttributes(int position){
        myViewHolder.restaurantNameTextView.setText(currentRestaurant.getName());

        showRate();
        showYellowStar();
        showHowFarFrom();
        showRestaurantImage();
        showRestaurantOpeningHours();
        showRestaurantFoodCountryAndAddress();
        saveRestaurantClickedAndStartDetailsActivity(position);
    }

    private void showRate(){
        int interested = 0;
        if (currentRestaurant.getNumberOfInterestedWorkmate() != 0)
            interested = currentRestaurant.getNumberOfInterestedWorkmate();
        myViewHolder.numberOfInterestedWorkmateTextView.setText(MessageFormat.format("({0})", interested));
    }
    private void showYellowStar(){
        int rate = currentRestaurant.getFavorableOpinion();
        if (rate == 1)
            myViewHolder.yellowStar1.setVisibility(View.VISIBLE);
        else if(rate == 2){
            myViewHolder.yellowStar1.setVisibility(View.VISIBLE);
            myViewHolder.yellowStar2.setVisibility(View.VISIBLE);
        }
        else if ( rate >= 3){
            myViewHolder.yellowStar1.setVisibility(View.VISIBLE);
            myViewHolder.yellowStar2.setVisibility(View.VISIBLE);
            myViewHolder.yellowStar3.setVisibility(View.VISIBLE);
        }
    }
    private void showHowFarFrom(){
        String distanceFromDeviceLocation = String.format("%sm", currentRestaurant.getDistanceFromDeviceLocation());
        myViewHolder.howFarFromRestaurantTextView.setText(distanceFromDeviceLocation);
    }
    private void showRestaurantImage(){
        if (currentRestaurant.getImageUrl() != null)
            Picasso.get().load(currentRestaurant.getImageUrl())
                    .placeholder(android.R.drawable.stat_sys_download)
                    .error(android.R.drawable.stat_notify_error)
                    .resize(154, 154)
                    .into(myViewHolder.restaurantImage);
    }
    private void showRestaurantOpeningHours(){
        MyOpeningHours myOpeningHours = currentRestaurant.getOpeningHours();
        if (myOpeningHours != null){
            String status = myOpeningHours.getOpeningStatus();

            if (status.equals(Constants.CLOSING_SOON))
                myViewHolder.closeTimeTextView.setTextAppearance(R.style.closing_soon_style);
            else
                myViewHolder.closeTimeTextView.setTextAppearance(R.style.no_closing_soon_style);
            myViewHolder.closeTimeTextView.setText(status);
        }
    }
    private void showRestaurantFoodCountryAndAddress(){
        String foodCountry = currentRestaurant.getFoodCountry();
        if (foodCountry != null)
            myViewHolder.foodCountryAndAddressTextView.setText(String.format("%s - %s", foodCountry, currentRestaurant.getAddress()));
        else
            myViewHolder.foodCountryAndAddressTextView.setText(String.format("%s",currentRestaurant.getAddress()));
    }
    private void saveRestaurantClickedAndStartDetailsActivity(int position){
        myViewHolder.itemView.setOnClickListener(v -> {
            RestaurantSelectedApi.getInstance().setRestaurantSelected(restaurantList.get(position));
            activity.startActivity(new Intent(activity, RestaurantDetailsActivity.class));
        });
    }

    @Override
    public int getItemCount() {
        if (restaurantList != null)
            return restaurantList.size();
        else
            return 0;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView yellowStar1;
        private ImageView yellowStar2;
        private ImageView yellowStar3;
        private ImageView restaurantImage;
        private TextView closeTimeTextView;
        private TextView restaurantNameTextView;
        private TextView howFarFromRestaurantTextView;
        private TextView foodCountryAndAddressTextView;
        private TextView numberOfInterestedWorkmateTextView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            setReferences(itemView);

            setStarsVisibilityToGone();
        }

        private void setStarsVisibilityToGone() {
            yellowStar1.setVisibility(View.GONE);
            yellowStar2.setVisibility(View.GONE);
            yellowStar3.setVisibility(View.GONE);
        }

        private void setReferences(View itemView) {
            yellowStar1 = itemView.findViewById(R.id.yellow_star_1);
            yellowStar2 = itemView.findViewById(R.id.yellow_star_2);
            yellowStar3 = itemView.findViewById(R.id.yellow_star_3);
            restaurantImage = itemView.findViewById(R.id.restaurant_image_view);
            closeTimeTextView = itemView.findViewById(R.id.close_time_text_view);
            howFarFromRestaurantTextView = itemView.findViewById(R.id.how_far_text_view);
            restaurantNameTextView = itemView.findViewById(R.id.restaurant_name_text_view);
            foodCountryAndAddressTextView = itemView.findViewById(R.id.food_country_and_address_text_view);
            numberOfInterestedWorkmateTextView = itemView.findViewById(R.id.number_of_interested_workmate);
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String url = RestaurantListUrlApi.getInstance(activity).getUrlThroughDeviceLocation();
                FilterResults filterResults = new FilterResults();

                RestaurantNearbyBank2.getInstance(activity.getApplication()).getRestaurantList(url,
                        restaurantList -> filterResults.values = getFilteredList(constraint, restaurantList));

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                restaurantList.clear();

                if (results.values != null)
                    restaurantList.addAll((List<Restaurant>) results.values);

                notifyDataSetChanged();
            }
        };
    }

    private List<Restaurant> getFilteredList(CharSequence constraint, List<Restaurant> restaurantListToFiltered){
        List<Restaurant> restaurantListFiltered = new ArrayList<>();
        String searchText = constraint.toString().toLowerCase().trim();

            if (constraint.length() == 0)
                restaurantListFiltered.addAll(restaurantListToFiltered);
            else {
                for (Restaurant restaurant : restaurantListToFiltered)
                    if (restaurant.getName().toLowerCase().contains(searchText))
                        restaurantListFiltered.add(restaurant);
            }

            return restaurantListFiltered;
    }

}
