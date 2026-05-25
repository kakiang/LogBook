package com.hervekakiang.logbook.matiere;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.hervekakiang.logbook.OnItemClickListener;
import com.hervekakiang.logbook.R;

public class MatiereListFragment extends Fragment {

    private MatiereListAdapter mAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_matiere_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar fragmentToolbar = view.findViewById(R.id.fragmentToolbar);
        NavController navController = Navigation.findNavController(view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupWithNavController(fragmentToolbar, navController, appBarConfiguration);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerviewMatiere);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        ExtendedFloatingActionButton fab = view.findViewById(R.id.fab_add_matiere);
        fab.setOnClickListener(v -> Navigation.findNavController(requireActivity(), R.id.navHostFragment).navigate(R.id.action_to_ajouterMatiereFragment));

        mAdapter = new MatiereListAdapter();
        recyclerView.setAdapter(mAdapter);

        OnItemClickListener<MatiereListAdapter.MatiereWithStats> listener = new OnItemClickListener<>() {
            @Override
            public void onItemClick(MatiereListAdapter.MatiereWithStats matiereWithStats) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.navHostFragment);
                Bundle args = new Bundle();
                args.putInt("matiereId", matiereWithStats.matiere().getId());
                args.putString("fragmentTitle", matiereWithStats.matiere().getNom());
                navController.navigate(R.id.matiereDetailFragment, args);
            }

            @Override
            public void onItemLongClick(MatiereListAdapter.MatiereWithStats obj) {

            }
        };
        mAdapter.setOnItemClickListener(listener);

        MatiereViewModel mViewModel = new ViewModelProvider(requireActivity()).get(MatiereViewModel.class);
        mViewModel.setCurrentUeId(0);
        mViewModel.getMatieresWithStats().observe(getViewLifecycleOwner(), matieres -> {
            mAdapter.submitList(matieres);
        });
    }
}