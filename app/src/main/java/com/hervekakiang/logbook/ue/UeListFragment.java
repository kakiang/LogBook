package com.hervekakiang.logbook.ue;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hervekakiang.logbook.R;
import com.hervekakiang.logbook.matiere.Matiere;
import com.hervekakiang.logbook.matiere.MatiereViewModel;
import com.hervekakiang.logbook.seance.Seance;
import com.hervekakiang.logbook.seance.SeanceViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddUeFragment addUeFragment = new AddUeFragment();
                addUeFragment.show(getChildFragmentManager(), AddUeFragment.class.getCanonicalName());
            }
        });

        UEListAdapter.OnItemClickListener listener = new UEListAdapter.OnItemClickListener() {
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
        ueViewModel.getUeUiModels().observe(getViewLifecycleOwner(), ueUiModels -> {
            mAdapter.submitList(Objects.requireNonNullElseGet(ueUiModels, ArrayList::new));
        });
    }
}