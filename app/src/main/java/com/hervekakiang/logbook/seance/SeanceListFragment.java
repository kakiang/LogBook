package com.hervekakiang.logbook.seance;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hervekakiang.logbook.R;
import com.hervekakiang.logbook.matiere.Matiere;
import com.hervekakiang.logbook.matiere.MatiereViewModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeanceListFragment extends Fragment {

    private SeanceListGroupAdaper seanceListGroupAdaper;
    private List<Seance> seances = new ArrayList<>();
    private List<Matiere> matieres = new ArrayList<>();

    public static SeanceListFragment newInstance() {
        return new SeanceListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_seance_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar fragmentToolbar = view.findViewById(R.id.fragmentToolbar);
        NavController navController = Navigation.findNavController(view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupWithNavController(fragmentToolbar, navController, appBarConfiguration);


        RecyclerView recyclerView = view.findViewById(R.id.recyclerviewSeance);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ExtendedFloatingActionButton fab = view.findViewById(R.id.fabAddSeance);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AjouterSeanceFragment ajouterSeanceFragment = new AjouterSeanceFragment();
                ajouterSeanceFragment.show(getChildFragmentManager(), AjouterSeanceFragment.class.getCanonicalName());
            }
        });

        seanceListGroupAdaper = new SeanceListGroupAdaper();
        recyclerView.setAdapter(seanceListGroupAdaper);

        MatiereViewModel matiereViewModel = new ViewModelProvider(requireActivity()).get(MatiereViewModel.class);
        SeanceViewModel seanceViewModel = new ViewModelProvider(requireActivity()).get(SeanceViewModel.class);
        matiereViewModel.setCurrentUeId(0);
        seanceViewModel.setMatiereId(0);
        matiereViewModel.getListMatieres().observe(getViewLifecycleOwner(), matieres -> {
            this.matieres = matieres;
            groupSeances();
        });

        seanceViewModel.getListSeances().observe(getViewLifecycleOwner(), seances -> {
            this.seances = seances;
            groupSeances();
        });
    }

    private void groupSeances() {
        if (matieres.isEmpty() || seances.isEmpty()) return;
        Map<Integer, String> matiereMap = new HashMap<>();
        for (Matiere m : matieres) matiereMap.put(m.getId(), m.getNom());
//        seances.sort((s1, s2) -> Integer.compare(s1.getMatiereId(), s2.getMatiereId()));
        seances.sort(Comparator.comparingInt(Seance::getMatiereId));

        List<SeanceListItem> items = new ArrayList<>();
        int lastMatiereId = -1;

        for (Seance s : seances) {
            if (s.getMatiereId() != lastMatiereId) {
                items.add(new SeanceListItem(matiereMap.get(s.getMatiereId()), s.getMatiereId()));
                lastMatiereId = s.getMatiereId();
            }
            items.add(new SeanceListItem(s));
        }
        seanceListGroupAdaper.submitList(items);
    }
}