package com.example.go4lunch.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.go4lunch.R;
import com.example.go4lunch.adapter.WorkmateRecyclerViewAdapter;
import com.example.go4lunch.model.Restaurant;
import com.example.go4lunch.model.Workmate;
import com.example.go4lunch.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class WorkmateListViewFragment extends Fragment {

    private RecyclerView recyclerView;

    private WorkmateRecyclerViewAdapter workmateAdapter;
    private final List<Workmate> workmateList = Constants.getWorkmateList();

    public WorkmateListViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workmate_list_view, container, false);

        recyclerView = view.findViewById(R.id.workmate_list_recycler_view);

//        List<Workmate> workmateList = new ArrayList<>();
        Context context = view.getContext();

        workmateAdapter = new WorkmateRecyclerViewAdapter(context, workmateList);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(workmateAdapter);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_view_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search_item);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setQueryHint(Constants.SEARCH_WORKMATES_TEXT);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Action after user validate his search text
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Real time action
//                workmateAdapter.getFilter().filter(newText);
                filterList(newText);
                return false;
            }
        });

    }

    private void filterList(String query){
        List<Workmate> listFiltered = new ArrayList<>();

        for (Workmate workmate : workmateList) {
            Restaurant restaurantChosen = workmate.getRestaurantChosen();
            if (restaurantChosen != null){
                String restaurantChosenName = restaurantChosen.getName();
                if (restaurantChosenName.toLowerCase().contains(query.toLowerCase()))
                    listFiltered.add(workmate);
            }
        }

        workmateAdapter = new WorkmateRecyclerViewAdapter(getContext(), listFiltered);
        recyclerView.setAdapter(workmateAdapter);
        workmateAdapter.notifyDataSetChanged();
    }
}