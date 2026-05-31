package com.hervekakiang.logbook.matiere;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.hervekakiang.logbook.BaseFragment;
import com.hervekakiang.logbook.MyAppViewModel;
import com.hervekakiang.logbook.OnItemClickListener;
import com.hervekakiang.logbook.R;
import com.hervekakiang.logbook.seance.Seance;
import com.hervekakiang.logbook.seance.SeanceListAdaper;

import java.util.Locale;

public class MatiereDetailFragment extends BaseFragment {

    private int matiereId;

    public MatiereDetailFragment() {
        super(R.layout.fragment_matiere_detail);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null && getArguments().containsKey("matiereId")) {
            matiereId = getArguments().getInt("matiereId");
            Log.d("MYAPP::MatiereDetail", "matiereId=" + matiereId);
        } else {
            Log.d("MYAPP::MatiereDetail", "matiereId=null");
            getNavController().navigateUp();
        }

        ProgressBar progressBar = view.findViewById(R.id.chartProgress);
        TextView tvChartPercentage = view.findViewById(R.id.tvChartPercentage);
        TextView textViewVhStat = view.findViewById(R.id.textViewVhStat);
        TextView tvSeanceListTitle = view.findViewById(R.id.tvSeanceListTitle);
        TextView tvEnseignant = view.findViewById(R.id.tvEnseignant);
        ExtendedFloatingActionButton fab = view.findViewById(R.id.fabAddSeance);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerviewSeance);
        TextView layoutEmpty = view.findViewById(R.id.layoutEmpty);


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        SeanceListAdaper mAdapter = new SeanceListAdaper();
        mAdapter.setOnItemClickListener(listener());
        recyclerView.setAdapter(mAdapter);

        MyAppViewModel myAppViewModel = new ViewModelProvider(requireActivity()).get(MyAppViewModel.class);
        myAppViewModel.setCurrentMatiereId(matiereId);
        myAppViewModel.getCurrentMatiereDTO().observe(getViewLifecycleOwner(), matiereWithStats -> {
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
            String enseignant = "Enseignant " + matiereWithStats.matiere().getEnseignant();
            tvEnseignant.setText(enseignant);
        });


        fab.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("selectedMatiereId", matiereId);
            getNavController().navigate(R.id.ajouterSeanceFragment, args);
        });

        myAppViewModel.getListSeanceForCurrentMatiere().observe(getViewLifecycleOwner(), seances -> {
            String seanceListTitle = "Seances de cours (" + seances.size() + ")";
            tvSeanceListTitle.setText(seanceListTitle);
            if (seances.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                layoutEmpty.setVisibility(View.GONE);
                mAdapter.submitList(seances);
            }
        });
    }

    private OnItemClickListener<Seance> listener() {
        return  new OnItemClickListener<>() {
            @Override
            public void onItemClick(Seance s) {
                Bundle args = new Bundle();
                args.putInt("seanceId", s.getId());
                args.putSerializable("seance", s);
                getNavController().navigate(R.id.seanceDetailFragment, args);
            }

            @Override
            public void onItemLongClick(Seance obj) {

            }
        };
    }
}