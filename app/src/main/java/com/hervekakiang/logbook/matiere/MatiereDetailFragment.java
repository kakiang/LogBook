package com.hervekakiang.logbook.matiere;

import android.animation.ObjectAnimator;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hervekakiang.logbook.R;
import com.hervekakiang.logbook.ViewModelFactory;
import com.hervekakiang.logbook.seance.AjouterSeanceFragment;
import com.hervekakiang.logbook.seance.SeanceListAdaper;
import com.hervekakiang.logbook.seance.SeanceListGroupAdaper;
import com.hervekakiang.logbook.seance.SeanceViewModel;

import java.util.Locale;

public class MatiereDetailFragment extends Fragment {

    private MatiereListAdapter.MatiereWithStats matiereWithStats;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_matiere_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getArguments()!=null) {
            matiereWithStats = (MatiereListAdapter.MatiereWithStats) getArguments().getSerializable("matiereWithStats");
        }

        ProgressBar progressBar = view.findViewById(R.id.chartProgress);
        TextView tvChartPercentage = view.findViewById(R.id.tvChartPercentage);
        TextView textViewVhStat = view.findViewById(R.id.textViewVhStat);
        TextView tvSeanceListTitle = view.findViewById(R.id.tvSeanceListTitle);
        TextView tvEnseignant = view.findViewById(R.id.tvEnseignant);

        if (matiereWithStats != null) {
            ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", 0, matiereWithStats.pourcentage());
            animator.setDuration(1500);
            animator.setInterpolator(new FastOutSlowInInterpolator());
            animator.start();

            tvChartPercentage.setText(String.format(Locale.getDefault(), "%d%%", matiereWithStats.pourcentage()));
            String vhStat = matiereWithStats.volumeHoraireStat();
            vhStat = vhStat.replace("/", "dispensées /");
            textViewVhStat.setText(vhStat);
            String enseignant = "Enseignant : "+matiereWithStats.matiere().getEnseignant();
            tvEnseignant.setText(enseignant);
        }

        FloatingActionButton fab = view.findViewById(R.id.fabAddSeance);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AjouterSeanceFragment ajouterSeanceFragment = new AjouterSeanceFragment();
                ajouterSeanceFragment.show(getChildFragmentManager(), AjouterSeanceFragment.class.getCanonicalName());
            }
        });

        RecyclerView recyclerView = view.findViewById(R.id.recyclerviewSeance);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        SeanceListAdaper mAdapter = new SeanceListAdaper();
        recyclerView.setAdapter(mAdapter);

        ViewModelFactory factory = new ViewModelFactory(requireActivity().getApplication(), matiereWithStats.matiere().getId());
        SeanceViewModel seanceViewModel = new ViewModelProvider(this, factory).get(SeanceViewModel.class);
        seanceViewModel.getListSeances().observe(getViewLifecycleOwner(), seances -> {
            String seanceListTitle = "Seances de cours (" + seances.size() + ")";
            tvSeanceListTitle.setText(seanceListTitle);
            mAdapter.submitList(seances);
        });
    }
}