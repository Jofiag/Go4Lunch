package com.example.go4lunch.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.go4lunch.R;
import com.example.go4lunch.model.Restaurant;
import com.example.go4lunch.model.Workmate;
import com.example.go4lunch.util.Constants;

import java.text.MessageFormat;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class WorkmateRecyclerViewAdapter extends RecyclerView.Adapter<WorkmateRecyclerViewAdapter.MyViewHolder> {

    public interface OnWorkmateClickListener {
        void onWorkmateSelected(Workmate workmate);
    }

    private Context context;
    private final List<Workmate> workmateList;
    private OnWorkmateClickListener mCallback;

    public WorkmateRecyclerViewAdapter(Context context, List<Workmate> workmateList) {
        this.context = context;
        this.workmateList = workmateList;
        this.mCallback = (OnWorkmateClickListener) context;
    }

    public WorkmateRecyclerViewAdapter(List<Workmate> workmateList) {
        this.workmateList = workmateList;
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

        Restaurant restaurant = workmate.getRestaurantChosen();
        if (restaurant != null){
            holder.foodCountryTextView.setText(restaurant.getFoodCountry());
            holder.restaurantNameTextView.setText(MessageFormat.format("({0})", restaurant.getName()));
            holder.workmateNameTextView.setText(workmate.getName());
        }
        else {
            holder.isEatingTextView.setVisibility(View.GONE);
            holder.foodCountryTextView.setVisibility(View.GONE);
            holder.restaurantNameTextView.setVisibility(View.GONE);
            holder.workmateNameTextView.setVisibility(View.GONE);
            holder.hasNotDecidedTextView.setText(String.format("%s%s", workmate.getName(), Constants.HAS_NOT_DECIDED_YET));
        }
        holder.circleImageView.setImageURI(workmate.getImageUri());

        holder.itemView.setOnClickListener(v -> mCallback.onWorkmateSelected(workmate));
    }

    @Override
    public int getItemCount() {
        if (workmateList != null)
            return workmateList.size();
        else
            return 0;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView foodCountryTextView;
        private TextView workmateNameTextView;
        private TextView restaurantNameTextView;
        private TextView isEatingTextView;
        private TextView hasNotDecidedTextView;
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
            isEatingTextView = itemView.findViewById(R.id.is_eating);
            hasNotDecidedTextView = itemView.findViewById(R.id.has_not_decided_text_view);
        }
    }
}
