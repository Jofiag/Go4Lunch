package com.example.go4lunch.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.go4lunch.R;
import com.example.go4lunch.controller.RestaurantDetailsActivity;
import com.example.go4lunch.data.RestaurantListUrlApi;
import com.example.go4lunch.data.RestaurantNearbyBank;
import com.example.go4lunch.data.RestaurantSelectedApi;
import com.example.go4lunch.model.MyOpeningHours;
import com.example.go4lunch.model.Restaurant;
import com.example.go4lunch.util.Constants;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RestaurantRecyclerViewAdapter extends RecyclerView.Adapter<RestaurantRecyclerViewAdapter.MyViewHolder>
implements Filterable {
    private final Context context;
    private final List<Restaurant> restaurantList;

    public RestaurantRecyclerViewAdapter(Context context, List<Restaurant> restaurantList) {
        this.context = context;
        this.restaurantList = restaurantList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_row, parent, false);

        return new MyViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Restaurant restaurant = restaurantList.get(position);

        MyOpeningHours myOpeningHours = restaurant.getOpeningHours();
        if (myOpeningHours != null){
            String status = myOpeningHours.getOpeningStatus();

            if (status.equals(Constants.CLOSING_SOON))
                holder.closeTimeTextView.setTextAppearance(R.style.closing_soon_style);
            else
                holder.closeTimeTextView.setTextAppearance(R.style.no_closing_soon_style);

            holder.closeTimeTextView.setText(status);
        }

        String foodCountry = restaurant.getFoodCountry();
        if (foodCountry != null)
            holder.foodCountryAndAddressTextView.setText(String.format("%s - %s", foodCountry, restaurant.getAddress()));
        else
            holder.foodCountryAndAddressTextView.setText(String.format("%s",restaurant.getAddress()));


        if (restaurant.getImageUrl() != null)
            Picasso.get().load(restaurant.getImageUrl())
                    .placeholder(android.R.drawable.stat_sys_download)
                    .error(android.R.drawable.stat_notify_error)
                    .resize(154, 154)
                    .into(holder.restaurantImage);

        showYellowStar(holder, restaurant.getFavorableOpinion());

        String distanceFromDeviceLocation = String.format("%sm", restaurant.getDistanceFromDeviceLocation());
        holder.howFarFromRestaurantTextView.setText(distanceFromDeviceLocation);

        holder.restaurantNameTextView.setText(restaurant.getName());
        holder.numberOfInterestedWorkmateTextView.setText("(0)");
        holder.itemView.setOnClickListener(v -> {
            RestaurantSelectedApi.getInstance().setRestaurantSelected(restaurant);
            context.startActivity(new Intent(context, RestaurantDetailsActivity.class));
        });

    }

    @Override
    public int getItemCount() {
        if (restaurantList != null)
            return restaurantList.size();
        else
            return 0;
    }

    private void showYellowStar(MyViewHolder holder, int favorableOpinion){
        if (favorableOpinion == 1)
            holder.yellowStar1.setVisibility(View.VISIBLE);
        else if(favorableOpinion == 2){
            holder.yellowStar1.setVisibility(View.VISIBLE);
            holder.yellowStar2.setVisibility(View.VISIBLE);
        }
        else if ( favorableOpinion >= 3){
            holder.yellowStar1.setVisibility(View.VISIBLE);
            holder.yellowStar2.setVisibility(View.VISIBLE);
            holder.yellowStar3.setVisibility(View.VISIBLE);
        }
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

//    public void replaceList(List<Restaurant> newList){
//        restaurantList.clear();
//        restaurantList.addAll(newList);
//        notifyDataSetChanged();
//    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String url = RestaurantListUrlApi.getInstance(context).getUrlThroughDeviceLocation();
                FilterResults filterResults = new FilterResults();

                RestaurantNearbyBank.getInstance(context, null).getRestaurantNearbyList(url,
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
