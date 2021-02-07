package com.example.go4lunch.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.go4lunch.R;
import com.example.go4lunch.model.Restaurant;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class RestaurantRecyclerViewAdapter extends RecyclerView.Adapter<RestaurantRecyclerViewAdapter.MyViewHolder> {

    public interface OnRestaurantClickListener{
        void onRestaurantSelected(Restaurant restaurant);
    }

    private final OnRestaurantClickListener mCallback;
    private List<Restaurant> restaurantList = new ArrayList<>();

    public RestaurantRecyclerViewAdapter(Context context, List<Restaurant> restaurantList) {
        this.restaurantList = restaurantList;
        this.mCallback = (OnRestaurantClickListener) context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_row, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Restaurant restaurant = restaurantList.get(position);

        holder.restaurantNameTextView.setText(restaurant.getName());
        holder.restaurantImage.setImageURI(restaurant.getImageUri());
        holder.howFarFromWorkmateTextView.setText(restaurant.getHowFarFromWorkmate());
        holder.closeTimeTextView.setText(restaurant.getOpeningHours().getOpeningStatus());
        holder.foodCountryAndAddressTextView.setText(String.format("%s - %s", restaurant.getFoodCountry(), restaurant.getAddress()));
        holder.numberOfInterestedWorkmateTextView.setText(MessageFormat.format("({0})", restaurant.getNumberOfInterestedWorkmate()));

        showYellowStar(holder, restaurant.getNumberOfFavorableOpinion());

        holder.itemView.setOnClickListener(v -> mCallback.onRestaurantSelected(restaurant));

    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    private void showYellowStar(MyViewHolder holder, int numberOfFavorableOpinion){
        if (numberOfFavorableOpinion == 1)
            holder.yellowStar1.setVisibility(View.VISIBLE);
        else if(numberOfFavorableOpinion == 2){
            holder.yellowStar1.setVisibility(View.VISIBLE);
            holder.yellowStar2.setVisibility(View.VISIBLE);
        }
        else if ( numberOfFavorableOpinion >= 3){
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
        private TextView howFarFromWorkmateTextView;
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
            howFarFromWorkmateTextView = itemView.findViewById(R.id.how_far_text_view);
            restaurantNameTextView = itemView.findViewById(R.id.restaurant_name_text_view);
            foodCountryAndAddressTextView = itemView.findViewById(R.id.food_country_and_address_text_view);
            numberOfInterestedWorkmateTextView = itemView.findViewById(R.id.number_of_interested_workmate);
        }
    }
}
