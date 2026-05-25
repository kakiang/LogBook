package com.hervekakiang.logbook.matiere;

import android.animation.ObjectAnimator;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.ViewModelProvider;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hervekakiang.logbook.R;
import com.hervekakiang.logbook.seance.AjouterSeanceFragment;
import com.hervekakiang.logbook.seance.SeanceListAdaper;
import com.hervekakiang.logbook.seance.SeanceViewModel;
import com.hervekakiang.logbook.ue.UEViewModel;

import java.util.Locale;

public class MatiereDetailFragment extends Fragment {

    private int matiereId;

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

        MaterialToolbar fragmentToolbar = view.findViewById(R.id.fragmentToolbar);
        NavController navController = Navigation.findNavController(view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupWithNavController(fragmentToolbar, navController, appBarConfiguration);

        if (getArguments() != null && getArguments().containsKey("matiereId")) {
            matiereId = getArguments().getInt("matiereId");
            Log.d("MYAPP::MatiereDetail", "matiereId=" + matiereId);
        } else {
            Log.d("MYAPP::MatiereDetail", "matiereId=null");
            navController.navigateUp();
        }

        ProgressBar progressBar = view.findViewById(R.id.chartProgress);
        TextView tvChartPercentage = view.findViewById(R.id.tvChartPercentage);
        TextView textViewVhStat = view.findViewById(R.id.textViewVhStat);
        TextView tvSeanceListTitle = view.findViewById(R.id.tvSeanceListTitle);
        TextView tvEnseignant = view.findViewById(R.id.tvEnseignant);
        ExtendedFloatingActionButton fab = view.findViewById(R.id.fabAddSeance);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerviewSeance);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        SeanceListAdaper mAdapter = new SeanceListAdaper();
        recyclerView.setAdapter(mAdapter);

        UEViewModel ueViewModel = new ViewModelProvider(requireActivity()).get(UEViewModel.class);
        ueViewModel.setCurrentMatiereId(matiereId);
        ueViewModel.getCurrentMatiereWithStats().observe(getViewLifecycleOwner(), matiereWithStats -> {
            Log.d("MYAPP::MatDetailF", "matiereWithStats=" + matiereWithStats);
            if (matiereWithStats == null) return;
            ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", 0, matiereWithStats.pourcentage());
            animator.setDuration(1000);
            animator.setInterpolator(new FastOutSlowInInterpolator());
            animator.start();

            tvChartPercentage.setText(String.format(Locale.getDefault(), "%d%%", matiereWithStats.pourcentage()));
            String vhStat = matiereWithStats.volumeHoraireStat();
            vhStat = vhStat.replace("/", "dispensées /");
            textViewVhStat.setText(vhStat);
            String enseignant = "Enseignant : " + matiereWithStats.matiere().getEnseignant();
            tvEnseignant.setText(enseignant);
        });


        fab.setOnClickListener(v -> {
            AjouterSeanceFragment ajouterSeanceFragment = new AjouterSeanceFragment(matiereId);
            ajouterSeanceFragment.show(getChildFragmentManager(), AjouterSeanceFragment.class.getCanonicalName());
        });

        ueViewModel.getSeancesForCurrentMatiere().observe(getViewLifecycleOwner(), seances -> {
            mAdapter.submitList(seances);
            String seanceListTitle = "Seances de cours (" + seances.size() + ")";
            tvSeanceListTitle.setText(seanceListTitle);
            mAdapter.submitList(seances);
        });
    }
}