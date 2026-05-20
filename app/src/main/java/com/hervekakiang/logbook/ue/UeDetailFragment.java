package com.hervekakiang.logbook.ue;

import android.animation.ObjectAnimator;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hervekakiang.logbook.OnItemClickListener;
import com.hervekakiang.logbook.R;
import com.hervekakiang.logbook.ViewModelFactory;
import com.hervekakiang.logbook.matiere.AjouterMatiereFragment;
import com.hervekakiang.logbook.matiere.MatiereListAdapter;
import com.hervekakiang.logbook.matiere.MatiereViewModel;

import java.util.Locale;

public class UeDetailFragment extends Fragment {
    private UEListAdapter.UeWithStats ueWithStats;

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
            ueWithStats = (UEListAdapter.UeWithStats) getArguments().getSerializable("ueModel");
        }
        ProgressBar progressBar = view.findViewById(R.id.chartProgress);
        TextView tvChartPercentage = view.findViewById(R.id.tvChartPercentage);
        TextView textViewVhStat = view.findViewById(R.id.textViewVhStat);
        TextView tvMatiereListTitle = view.findViewById(R.id.tvMatiereListTitle);

        if (ueWithStats != null) {
            ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", 0, ueWithStats.pourcentage());
            animator.setDuration(1500);
            animator.setInterpolator(new FastOutSlowInInterpolator());
            animator.start();

            tvChartPercentage.setText(String.format(Locale.getDefault(), "%d%%", ueWithStats.pourcentage()));
            textViewVhStat.setText(ueWithStats.volumeHoraireStat());
        }

        FloatingActionButton fab = view.findViewById(R.id.fabAddMatiere);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AjouterMatiereFragment ajouterMatiereFragment = new AjouterMatiereFragment(ueWithStats.ue().getId());
                ajouterMatiereFragment.show(getChildFragmentManager(), AjouterMatiereFragment.class.getCanonicalName());
            }
        });

        RecyclerView recyclerView = view.findViewById(R.id.recyclerviewMatiere);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        MatiereListAdapter mAdapter = getMatiereListAdapter();

        recyclerView.setAdapter(mAdapter);

        ViewModelFactory factory = new ViewModelFactory(requireActivity().getApplication(), ueWithStats.ue().getId());
        String key = "MatiereViewModel_" + ueWithStats.ue().getId();
        MatiereViewModel mViewModel = new ViewModelProvider(requireActivity(), factory).get(key, MatiereViewModel.class);

        Log.d("UEDETAIL mViewModel.getUeId()", String.valueOf(mViewModel.getUeId()));
        mViewModel.getMatieresWithStats().observe(getViewLifecycleOwner(), matieres -> {
            String mt = "Matières (" + matieres.size() + ")";
            tvMatiereListTitle.setText(mt);
            mAdapter.submitList(matieres);
        });

    }

    @NonNull
    private MatiereListAdapter getMatiereListAdapter() {
        MatiereListAdapter mAdapter = new MatiereListAdapter();

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
        return mAdapter;
    }
}