package com.hervekakiang.logbook.dashboard;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hervekakiang.logbook.R;
import com.hervekakiang.logbook.matiere.Matiere;
import com.hervekakiang.logbook.matiere.MatiereViewModel;
import com.hervekakiang.logbook.seance.Seance;
import com.hervekakiang.logbook.seance.SeanceViewModel;
import com.hervekakiang.logbook.ue.AddUeFragment;
import com.hervekakiang.logbook.ue.UEListAdapter;
import com.hervekakiang.logbook.ue.UEViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DashboardFragment extends Fragment {

    private DashboardViewModel mViewModel;
    private List<Seance> seances = new ArrayList<>();
    private List<Matiere> matieres = new ArrayList<>();

    private ProgressBar progressBar;
    private TextView tvChartPercentage;
    private TextView tvVhEffectue;
    private TextView tvVhRestant;

    public static DashboardFragment newInstance() {
        return new DashboardFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.chartProgress);
        tvChartPercentage = view.findViewById(R.id.tvChartPercentage);
        tvVhEffectue = view.findViewById(R.id.tvVhEffectue);
        tvVhRestant = view.findViewById(R.id.tvVhRestant);

        TextView textViewUeListTitle = view.findViewById(R.id.textViewUeListTitle);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerviewUE);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FloatingActionButton fab = view.findViewById(R.id.fab_add_ue);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddUeFragment addUeFragment = new AddUeFragment();
                addUeFragment.show(getChildFragmentManager(), AddUeFragment.class.getCanonicalName());
            }
        });

        MatiereViewModel mViewModel = new ViewModelProvider(requireActivity()).get(MatiereViewModel.class);
        mViewModel.getListMatieres().observe(getViewLifecycleOwner(), matieres -> {
            this.matieres = matieres;
            calculHoraire();
        });

        SeanceViewModel seanceViewModel = new ViewModelProvider(requireActivity()).get(SeanceViewModel.class);
        seanceViewModel.getListSeances().observe(getViewLifecycleOwner(), seances -> {
            this.seances = seances;
            calculHoraire();
        });

        UEListAdapter mAdapter = getUeListAdapter();
        recyclerView.setAdapter(mAdapter);

        UEViewModel ueViewModel = new ViewModelProvider(requireActivity()).get(UEViewModel.class);
        ueViewModel.getUeUiModels().observe(getViewLifecycleOwner(), ueUiModels -> {
            String uet = "Unités d'enseignement (" + ueUiModels.size() + ")";
            textViewUeListTitle.setText(uet);
            mAdapter.submitList(Objects.requireNonNullElseGet(ueUiModels, ArrayList::new));
        });

    }

    @NonNull
    private UEListAdapter getUeListAdapter() {
        UEListAdapter.OnItemClickListener listener = new UEListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(UEListAdapter.UeWithStats model) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.navHostFragment);
                Bundle args = new Bundle();
                args.putSerializable("ueModel", model);
                String fragmentTitle = model.ue().getCode() + " " + model.ue().getNom();
                args.putString("fragmentTitle", fragmentTitle);
                navController.navigate(R.id.ueDetailFragment, args);
            }

            @Override
            public void onItemLongClick(UEListAdapter.UeWithStats uiModel) {

            }
        };

        UEListAdapter mAdapter = new UEListAdapter();
        mAdapter.setOnItemClickListener(listener);
        return mAdapter;
    }

    private void calculHoraire() {
        if (matieres.isEmpty() || seances.isEmpty()) return;
        int horaireEffectue = 0;
        int horaireTotal = 0;

        for (Matiere m : matieres) {
            horaireTotal += m.getVolumeHoraire();
        }
        for (Seance s : seances) {
            horaireEffectue += s.getDuree();
        }

        tvVhEffectue.setText(String.format(Locale.getDefault(), "%dh", horaireEffectue));
        tvVhRestant.setText(String.format(Locale.getDefault(), "%dh", horaireTotal - horaireEffectue));

        int percentage = (horaireTotal > 0) ? (horaireEffectue * 100) / horaireTotal : 0;
        progressBar.setProgress(percentage);
        tvChartPercentage.setText(String.format(Locale.getDefault(), "%d%%", percentage));
    }
}