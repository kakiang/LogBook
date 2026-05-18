package com.hervekakiang.logbook.ue;

import android.animation.ObjectAnimator;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hervekakiang.logbook.R;
import com.hervekakiang.logbook.matiere.Matiere;
import com.hervekakiang.logbook.matiere.MatiereListAdapter;
import com.hervekakiang.logbook.matiere.MatiereViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UeDetailFragment extends Fragment {
    private UEListAdapter.UeWithStats model;
    private TextView tvMatiereListTitle;
    private List<Matiere> matieres = new ArrayList<>();

    private ProgressBar progressBar;
    private TextView tvChartPercentage;
    private TextView textViewVhStat;


    public UeDetailFragment() {
    }

    public static UeDetailFragment newInstance(UE ue) {
        UeDetailFragment fragment = new UeDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("ue", ue);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ue_detail, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            model = (UEListAdapter.UeWithStats) getArguments().getSerializable("ueModel");
        }
        progressBar = view.findViewById(R.id.chartProgress);
        tvChartPercentage = view.findViewById(R.id.tvChartPercentage);
        textViewVhStat = view.findViewById(R.id.textViewVhStat);
        tvMatiereListTitle = view.findViewById(R.id.tvMatiereListTitle);

        if (model != null) {
            ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", 0, model.pourcentage());
            animator.setDuration(1500);
            animator.setInterpolator(new FastOutSlowInInterpolator());
            animator.start();

            tvChartPercentage.setText(String.format(Locale.getDefault(), "%d%%", model.pourcentage()));
            textViewVhStat.setText(model.volumeHoraireStat());
        }

        RecyclerView recyclerView = view.findViewById(R.id.recyclerviewMatiere);
        MatiereListAdapter mAdapter = new MatiereListAdapter();
        recyclerView.setAdapter(mAdapter);

        MatiereViewModel.Factory factory = new MatiereViewModel.Factory(requireActivity().getApplication(), model.ue().getId());
        MatiereViewModel mViewModel = new ViewModelProvider(this, factory).get(MatiereViewModel.class);

        Log.d("UEDETAIL mViewModel.getUeId()", String.valueOf(mViewModel.getUeId()));
        mViewModel.getMatieresWithStats().observe(getViewLifecycleOwner(), matieres -> {
            String mt = "Matières (" + matieres.size() + ")";
            tvMatiereListTitle.setText(mt);
            mAdapter.submitList(matieres);
        });

    }
}