package com.hervekakiang.logbook.dashboard;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.hervekakiang.logbook.OnItemClickListener;
import com.hervekakiang.logbook.R;
import com.hervekakiang.logbook.ue.AddUeFragment;
import com.hervekakiang.logbook.ue.UEListAdapter;
import com.hervekakiang.logbook.ue.UEViewModel;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class DashboardFragment extends Fragment {

    private ProgressBar progressBar;
    private TextView tvChartPercentage;
    private TextView tvVhEffectue;
    private TextView tvVhRestant;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar fragmentToolbar = view.findViewById(R.id.fragmentToolbar);
        NavController navController = Navigation.findNavController(view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupWithNavController(fragmentToolbar, navController, appBarConfiguration);

        fragmentToolbar.inflateMenu(R.menu.top_app_bar_menu);
        fragmentToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_search) {
                Toast.makeText(getContext(), "Search this UE", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        progressBar = view.findViewById(R.id.chartProgress);
        tvChartPercentage = view.findViewById(R.id.tvChartPercentage);
        tvVhEffectue = view.findViewById(R.id.tvVhEffectue);
        tvVhRestant = view.findViewById(R.id.tvVhRestant);
        TextView textViewUeListTitle = view.findViewById(R.id.textViewUeListTitle);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerviewUE);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ExtendedFloatingActionButton fab = view.findViewById(R.id.fab_add_ue);

        UEViewModel ueViewModel = new ViewModelProvider(requireActivity()).get(UEViewModel.class);
        ueViewModel.getStatsGlobal().observe(getViewLifecycleOwner(), stats -> {
            if (stats == null) return;
            Log.d("MYAPP:====stats=====", stats.toString());
            tvVhEffectue.setText(String.format(Locale.getDefault(), "%dh", stats.effectue()));
            tvVhRestant.setText(String.format(Locale.getDefault(), "%dh", stats.total() - stats.effectue()));
            int percentage = (stats.total() > 0) ? (stats.effectue() * 100) / stats.total() : 0;
            progressBar.setProgress(percentage);
            tvChartPercentage.setText(String.format(Locale.getDefault(), "%d%%", percentage));
        });

        UEListAdapter mAdapter = new UEListAdapter();
        mAdapter.setOnItemClickListener(listener());
        recyclerView.setAdapter(mAdapter);

        ueViewModel.getUeWithStatsList().observe(getViewLifecycleOwner(), ueWithStatsList -> {
            String uet = "Unités d'enseignement (" + ueWithStatsList.size() + ")";
            textViewUeListTitle.setText(uet);
            mAdapter.submitList(Objects.requireNonNullElseGet(ueWithStatsList, ArrayList::new));
        });

        fab.setOnClickListener(v -> {
            AddUeFragment addUeFragment = new AddUeFragment();
            addUeFragment.show(getChildFragmentManager(), AddUeFragment.class.getCanonicalName());
        });

    }

    @NonNull
    private  OnItemClickListener<UEListAdapter.UeWithStats> listener() {
        return new OnItemClickListener<>() {
            @Override
            public void onItemClick(UEListAdapter.UeWithStats ueWithStats) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.navHostFragment);
                Bundle args = new Bundle();
                args.putSerializable("ueWithStats", ueWithStats);
                String fragmentTitle = ueWithStats.ue().getCode() + " " + ueWithStats.ue().getNom();
                args.putString("fragmentTitle", fragmentTitle);
                navController.navigate(R.id.ueDetailFragment, args);
            }

            @Override
            public void onItemLongClick(UEListAdapter.UeWithStats uiModel) {

            }
        };
    }
}