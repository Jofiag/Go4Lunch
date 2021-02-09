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
import com.example.go4lunch.adapter.WorkmateRecyclerViewAdapter;
import com.example.go4lunch.util.Constants;

public class WorkmateListViewFragment extends Fragment {

    public WorkmateListViewFragment() {
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
        View view = inflater.inflate(R.layout.fragment_workmate_list_view, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.workmate_list_recycler_view);

//        List<Workmate> workmateList = new ArrayList<>();
        Context context = view.getContext();

        WorkmateRecyclerViewAdapter workmateAdapter = new WorkmateRecyclerViewAdapter(context, Constants.getWorkmateList());

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(workmateAdapter);

        return view;
    }
}