package com.hervekakiang.logbook.seance;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.hervekakiang.logbook.MyAppViewModel;
import com.hervekakiang.logbook.OnItemClickListener;
import com.hervekakiang.logbook.R;

public class SeanceListFragment extends Fragment {

    private SeanceListAdaper seanceListAdaper;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_seance_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);

        SearchBar searchBar = view.findViewById(R.id.search_bar);
        SearchView searchView = view.findViewById(R.id.search_view);

        searchView.setupWithSearchBar(searchBar);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerviewSeance);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        RecyclerView recyclerViewSearch = view.findViewById(R.id.recyclerviewSearchSeance);
        recyclerViewSearch.setLayoutManager(new LinearLayoutManager(getContext()));

        ExtendedFloatingActionButton fab = view.findViewById(R.id.fabAddSeance);
        fab.setOnClickListener(v -> {
            navController.navigate(R.id.ajouterSeanceFragment);
        });

        seanceListAdaper = new SeanceListAdaper();
        seanceListAdaper.setOnItemClickListener(listener(navController));
        recyclerView.setAdapter(seanceListAdaper);
        recyclerViewSearch.setAdapter(seanceListAdaper);

        MyAppViewModel viewModel = new ViewModelProvider(requireActivity()).get(MyAppViewModel.class);
        viewModel.getFilteredSeances().observe(getViewLifecycleOwner(), seances -> {
            seanceListAdaper.submitList(seances);
        });

        searchView.addTransitionListener((sv, previousState, newState) -> {
            if (newState == SearchView.TransitionState.SHOWING) {
                fab.hide();
            } else if (newState == SearchView.TransitionState.HIDDEN) {
                fab.show();
                viewModel.setSeanceSearchQuery("");
            }
        });


        searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> {
                    if (isAdded()) {
                        viewModel.setSeanceSearchQuery(s.toString());
                    }
                };
                searchHandler.postDelayed(searchRunnable, 200);
            }
        });
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (searchView.isShowing()) {
                    searchView.hide();
                } else {
                    setEnabled(false);
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private OnItemClickListener<Seance> listener(NavController navController) {
        return  new OnItemClickListener<>() {
            @Override
            public void onItemClick(Seance obj) {
                Bundle args = new Bundle();
                args.putInt("seanceId", obj.getId());
                args.putSerializable("seance", obj);
                navController.navigate(R.id.seanceDetailFragment, args);
            }

            @Override
            public void onItemLongClick(Seance obj) {

            }
        };
    }
}