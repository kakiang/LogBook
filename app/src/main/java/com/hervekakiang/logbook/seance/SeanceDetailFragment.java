package com.hervekakiang.logbook.seance;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.hervekakiang.logbook.R;
import com.hervekakiang.logbook.ue.UEViewModel;

import java.util.Map;

public class SeanceDetailFragment extends Fragment {

    private int seanceId;
    private Seance seance;

    public SeanceDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_seance_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MaterialToolbar fragmentToolbar = view.findViewById(R.id.fragmentToolbar);
        NavController navController = Navigation.findNavController(view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupWithNavController(fragmentToolbar, navController, appBarConfiguration);


        if (getArguments() != null) {
            seanceId = getArguments().getInt("seanceId");
            seance = (Seance) getArguments().getSerializable("seance");
        } else {
            navController.popBackStack();
        }

        TextView tvMatiere = view.findViewById(R.id.textViewMatiere);
        TextView tvEnseignant = view.findViewById(R.id.textViewEnseignant);
        TextView tvDate = view.findViewById(R.id.textViewDate);
        TextView tvHeure = view.findViewById(R.id.textViewHeure);
        TextView tvDuree = view.findViewById(R.id.textViewDuree);
        TextView tvContenu = view.findViewById(R.id.textViewContenuPedagogique);

        UEViewModel ueViewModel = new ViewModelProvider(requireActivity()).get(UEViewModel.class);
        ueViewModel.setCurrentSeanceId(seanceId);

        ueViewModel.getCurrentSeanceObj().observe(getViewLifecycleOwner(), seanceObj -> {
            Log.d("MYAPP::SEANCEDetailF", "seanceObj=" + seanceObj.get("matiere"));
            tvMatiere.setText(seanceObj.get("matiere"));
            tvEnseignant.setText(seanceObj.get("enseignant"));
            tvDate.setText(seanceObj.get("date"));
            tvHeure.setText(seanceObj.get("heure_debut"));
            String duree = seanceObj.get("duree") + "h";
            tvDuree.setText(duree);
            tvContenu.setText(seanceObj.get("contenu_pedagogique"));
        });

        view.findViewById(R.id.btnSeanceDelete).setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Suppression")
                    .setMessage("Êtes-vous sûr de vouloir supprimer cette seance")
                    .setNegativeButton("NON", (dialog, which) -> {

                    })
                    .setPositiveButton("OUI", (dialog, which) -> {
                        ueViewModel.deleteSeance(seanceId);
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(),
                                    "Seance supprimée avec succès",
                                    Toast.LENGTH_LONG).show();
                            navController.popBackStack();
                        });
                    }).show();
        });

        view.findViewById(R.id.btnSeanceEdit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putInt("selectedMatiereId", seance.getMatiereId());
                args.putInt("seanceId", seanceId);
                args.putBoolean("isEditing", true);
                navController.navigate(R.id.ajouterSeanceFragment, args);
            }
        });
    }
}