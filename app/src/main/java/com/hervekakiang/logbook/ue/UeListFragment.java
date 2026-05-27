package com.hervekakiang.logbook.ue;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hervekakiang.logbook.OnItemClickListener;
import com.hervekakiang.logbook.R;

import java.util.ArrayList;
import java.util.Objects;

public class UeListFragment extends Fragment {

    private UEListAdapter mAdapter;

    public static UeListFragment newInstance() {
        return new UeListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ue_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerviewUE);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FloatingActionButton fab = view.findViewById(R.id.fab_add_ue);
        fab.setOnClickListener(v -> Navigation.findNavController(requireActivity(), R.id.navHostFragment).navigate(R.id.ajouterUeFragment));

        OnItemClickListener<UEListAdapter.UeWithStats> listener = new OnItemClickListener<>() {
            @Override
            public void onItemClick(UEListAdapter.UeWithStats ueWithStats) {
                UeDetailFragment ueDetailFragment = UeDetailFragment.newInstance(ueWithStats.ue());
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.navHostFragment, ueDetailFragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onItemLongClick(UEListAdapter.UeWithStats ue) {

            }
        };
        mAdapter = new UEListAdapter();
        mAdapter.setOnItemClickListener(listener);
        recyclerView.setAdapter(mAdapter);

        UEViewModel ueViewModel = new ViewModelProvider(requireActivity()).get(UEViewModel.class);
        ueViewModel.getListUEWithStats().observe(getViewLifecycleOwner(), ueUiModels -> {
            mAdapter.submitList(Objects.requireNonNullElseGet(ueUiModels, ArrayList::new));
        });
    }
}