package com.hervekakiang.logbook.matiere;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hervekakiang.logbook.OnItemClickListener;
import com.hervekakiang.logbook.R;
import com.hervekakiang.logbook.ue.UE;
import com.hervekakiang.logbook.ue.UEViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatiereListFragment extends Fragment {

    private MatiereListAdapter mAdapter;

    public static MatiereListFragment newInstance() {
        return new MatiereListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_matiere_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerviewMatiere);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FloatingActionButton fab = view.findViewById(R.id.fab_add_matiere);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AjouterMatiereFragment ajouterMatiereFragment = new AjouterMatiereFragment();
                ajouterMatiereFragment.show(getChildFragmentManager(), AjouterMatiereFragment.class.getCanonicalName());
            }
        });

        mAdapter = new MatiereListAdapter();
        recyclerView.setAdapter(mAdapter);

        OnItemClickListener<MatiereListAdapter.MatiereWithStats> listener = new OnItemClickListener<MatiereListAdapter.MatiereWithStats>() {
            @Override
            public void onItemClick(MatiereListAdapter.MatiereWithStats matiereWithStats) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.navHostFragment);
                Bundle args = new Bundle();
                args.putSerializable("matiereWithStats", matiereWithStats);
                args.putString("fragmentTitle", matiereWithStats.matiere().getNom());
                navController.navigate(R.id.matiereDetailFragment, args);
            }

            @Override
            public void onItemLongClick(MatiereListAdapter.MatiereWithStats obj) {

            }
        };
        mAdapter.setOnItemClickListener(listener);

        MatiereViewModel mViewModel = new ViewModelProvider(requireActivity()).get(MatiereViewModel.class);
        mViewModel.getMatieresWithStats().observe(getViewLifecycleOwner(), matieres -> {
            mAdapter.submitList(matieres);
        });
    }
}