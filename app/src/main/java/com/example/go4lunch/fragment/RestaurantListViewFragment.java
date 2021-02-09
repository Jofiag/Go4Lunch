package com.example.go4lunch.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.go4lunch.R;
import com.example.go4lunch.adapter.RestaurantRecyclerViewAdapter;
import com.example.go4lunch.util.Constants;

public class RestaurantListViewFragment extends Fragment {

    public RestaurantListViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_list_view, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.restaurant_list_recycler_view);

//        List<Restaurant> restaurantList = new ArrayList<>();
        Context context = view.getContext();

        RestaurantRecyclerViewAdapter restaurantAdapter = new RestaurantRecyclerViewAdapter(context, Constants.getRestaurantList());

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(restaurantAdapter);

        return view;
    }
}