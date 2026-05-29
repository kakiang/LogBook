package com.hervekakiang.logbook.matiere;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

public class MatiereListFragment extends Fragment {

    private MatiereListAdapter mAdapter;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_matiere_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerview_matiere);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        RecyclerView recyclerViewSearch = view.findViewById(R.id.recyclerview_search_matiere);
        recyclerViewSearch.setLayoutManager(new LinearLayoutManager(getContext()));

        SearchBar searchBar = view.findViewById(R.id.search_bar);
        SearchView searchView = view.findViewById(R.id.search_view);

        searchView.setupWithSearchBar(searchBar);

        ExtendedFloatingActionButton fab = view.findViewById(R.id.fab_add_matiere);
        fab.setOnClickListener(v -> Navigation.findNavController(requireActivity(), R.id.navHostFragment).navigate(R.id.ajouterMatiereFragment));

        mAdapter = new MatiereListAdapter();
        recyclerView.setAdapter(mAdapter);
        recyclerViewSearch.setAdapter(mAdapter);

        OnItemClickListener<MatiereListAdapter.MatiereDTO> listener = new OnItemClickListener<>() {
            @Override
            public void onItemClick(MatiereListAdapter.MatiereDTO matiereWithStats) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.navHostFragment);
                Bundle args = new Bundle();
                args.putInt("matiereId", matiereWithStats.matiere().getId());
                args.putString("fragmentTitle", matiereWithStats.matiere().getNom());
                navController.navigate(R.id.matiereDetailFragment, args);
            }

            @Override
            public void onItemLongClick(MatiereListAdapter.MatiereDTO obj) {

            }
        };
        mAdapter.setOnItemClickListener(listener);

        MyAppViewModel viewModel = new ViewModelProvider(requireActivity()).get(MyAppViewModel.class);
        viewModel.getFilteredMatieres().observe(getViewLifecycleOwner(), matieres -> {
            mAdapter.submitList(matieres);
        });

        searchView.addTransitionListener((searchView1, previousState, newState) -> {
            if (newState == SearchView.TransitionState.SHOWING) {
                fab.hide();
            } else if (newState == SearchView.TransitionState.HIDDEN) {
                fab.show();
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
                        viewModel.setMatiereSearchQuery(s.toString());
                    }
                };
                searchHandler.postDelayed(searchRunnable, 200);

                Log.d("MatiereSearch MYAPP", s.toString());


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
}