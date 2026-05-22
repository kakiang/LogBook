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
import com.hervekakiang.logbook.OnItemClickListener;
import com.hervekakiang.logbook.R;
import com.hervekakiang.logbook.matiere.AjouterMatiereFragment;
import com.hervekakiang.logbook.matiere.MatiereListAdapter;

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
        return inflater.inflate(R.layout.fragment_ue_detail, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController navController = Navigation.findNavController(view);

        if (getArguments() != null) {
            ueWithStats = (UEListAdapter.UeWithStats) getArguments().getSerializable("ueWithStats");
        }else {
            navController.navigateUp();
        }

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        MaterialToolbar fragmentToolbar = view.findViewById(R.id.fragmentToolbar);
        NavigationUI.setupWithNavController(fragmentToolbar, navController, appBarConfiguration);

        ProgressBar progressBar = view.findViewById(R.id.chartProgress);
        TextView tvChartPercentage = view.findViewById(R.id.tvChartPercentage);
        TextView textViewVhStat = view.findViewById(R.id.textViewVhStat);
        TextView tvMatiereListTitle = view.findViewById(R.id.tvMatiereListTitle);
        fragmentToolbar.setTitle(ueWithStats.ue().getCode() + " " + ueWithStats.ue().getNom());

        UEViewModel ueViewModel = new ViewModelProvider(requireActivity()).get(UEViewModel.class);
        ueViewModel.setCurrentUeId(ueWithStats.ue().getId());

        ueViewModel.getCurrentUeWithStats().observe(getViewLifecycleOwner(), ueWithStats -> {
            ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", 0, ueWithStats.pourcentage());
            animator.setDuration(1000);
            animator.setInterpolator(new FastOutSlowInInterpolator());
            animator.start();

            tvChartPercentage.setText(String.format(Locale.getDefault(), "%d%%", ueWithStats.pourcentage()));
            textViewVhStat.setText(ueWithStats.volumeHoraireStat());
            fragmentToolbar.setTitle(ueWithStats.ue().getCode() + " " + ueWithStats.ue().getNom());
        });

        ExtendedFloatingActionButton fab = view.findViewById(R.id.fabAddMatiere);
        fab.setOnClickListener(v -> {
            AjouterMatiereFragment ajouterMatiereFragment = new AjouterMatiereFragment(ueWithStats.ue().getId());
            ajouterMatiereFragment.show(getChildFragmentManager(), AjouterMatiereFragment.class.getCanonicalName());
        });

        RecyclerView recyclerView = view.findViewById(R.id.recyclerviewMatiere);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        MatiereListAdapter mAdapter = new MatiereListAdapter();

        mAdapter.setOnItemClickListener(listener());
        recyclerView.setAdapter(mAdapter);

        ueViewModel.getMatieresWithStatsForCurrentUe().observe(getViewLifecycleOwner(), matiereWithStatsList -> {
            String mt = "Matières (" + matiereWithStatsList.size() + ")";
            tvMatiereListTitle.setText(mt);
            mAdapter.submitList(matiereWithStatsList);
        });

    }

    private OnItemClickListener<MatiereListAdapter.MatiereWithStats> listener() {
        return new OnItemClickListener<>() {
            @Override
            public void onItemClick(MatiereListAdapter.MatiereWithStats matiereWithStats) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.navHostFragment);
                Bundle args = new Bundle();
                Log.d("MYAPP::UeDetailFragment", "matiereId=" + matiereWithStats.matiere().getId());
                args.putInt("matiereId", matiereWithStats.matiere().getId());
                args.putString("fragmentTitle", matiereWithStats.matiere().getNom());
                navController.navigate(R.id.matiereDetailFragment, args);
            }

            @Override
            public void onItemLongClick(MatiereListAdapter.MatiereWithStats obj) {

            }
        };
    }
}