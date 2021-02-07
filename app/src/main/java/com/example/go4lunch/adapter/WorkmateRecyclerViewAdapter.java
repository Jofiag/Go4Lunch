package com.example.go4lunch.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.go4lunch.R;
import com.example.go4lunch.model.Workmate;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class WorkmateRecyclerViewAdapter extends RecyclerView.Adapter<WorkmateRecyclerViewAdapter.MyViewHolder> {

    public interface onWorkmateClickListener{
        void onWorkmateSelected(Workmate workmate);
    }

    private final List<Workmate> workmateList;
    private final onWorkmateClickListener mCallback;

    public WorkmateRecyclerViewAdapter(Context context, List<Workmate> workmateList) {
        this.workmateList = workmateList;
        this.mCallback = (onWorkmateClickListener) context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.workmate_row, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Workmate workmate = workmateList.get(position);

        holder.foodCountryTextView.setText(workmate.getRestaurantChosen().getFoodCountry());
        holder.workmateNameTextView.setText(workmate.getName());
        holder.restaurantNameTextView.setText(workmate.getRestaurantChosen().getName());
        holder.circleImageView.setImageURI(workmate.getImageUri());

        holder.itemView.setOnClickListener(v -> mCallback.onWorkmateSelected(workmate));

    }

    @Override
    public int getItemCount() {
        return workmateList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView foodCountryTextView;
        private TextView workmateNameTextView;
        private TextView restaurantNameTextView;
        private CircleImageView circleImageView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            setReferences(itemView);
        }

        private void setReferences(View itemView) {
            circleImageView = itemView.findViewById(R.id.workmate_image_view);
            workmateNameTextView = itemView.findViewById(R.id.workmate_name_text_view);
            foodCountryTextView = itemView.findViewById(R.id.food_country_text_view);
            restaurantNameTextView = itemView.findViewById(R.id.restaurant_name_workmate_row);
        }
    }
}
