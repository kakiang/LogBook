package com.hervekakiang.logbook.matiere;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hervekakiang.logbook.R;
import com.hervekakiang.logbook.ue.UE;
import com.hervekakiang.logbook.ue.UEViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatiereListFragment extends Fragment {

    private MatiereListAdapter mAdapter;
    private List<Matiere> matieres = new ArrayList<>();
    private List<UE> ues = new ArrayList<>();

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

        UEViewModel ueViewModel = new ViewModelProvider(requireActivity()).get(UEViewModel.class);
        MatiereViewModel mViewModel = new ViewModelProvider(requireActivity()).get(MatiereViewModel.class);

        ueViewModel.getListUEs().observe(getViewLifecycleOwner(), ues -> {
            this.ues = ues;
            combineAndSubmit();
        });

        mViewModel.getListMatieres().observe(getViewLifecycleOwner(), matieres -> {
            this.matieres = matieres;
            combineAndSubmit();
        });
    }

    private void combineAndSubmit() {
        if (matieres.isEmpty() || ues.isEmpty()) return;
        Map<Integer, String> ueMap = new HashMap<>();
        for(UE ue : ues) {
            ueMap.put(ue.getId(), ue.getNom());
        }

        List<Pair<Matiere, String>> combinedList = new ArrayList<>();
        for (Matiere m : matieres) {
            combinedList.add(new Pair<>(m, ueMap.get(m.getUeId())));
        }

        mAdapter.submitList(combinedList);
    }
}