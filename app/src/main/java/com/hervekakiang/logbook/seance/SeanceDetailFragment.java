package com.hervekakiang.logbook.seance;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.hervekakiang.logbook.BaseFragment;
import com.hervekakiang.logbook.MyAppViewModel;
import com.hervekakiang.logbook.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class SeanceDetailFragment extends BaseFragment {

    private int seanceId;
    private Seance seance;

    public SeanceDetailFragment() {
        super(R.layout.fragment_seance_detail);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            seanceId = getArguments().getInt("seanceId");
            seance = (Seance) getArguments().getSerializable("seance");
        } else {
            getNavController().popBackStack();
        }

        TextView tvMatiere = view.findViewById(R.id.textViewMatiere);
        TextView tvEnseignant = view.findViewById(R.id.textViewEnseignant);
        TextView tvDate = view.findViewById(R.id.textViewDate);
        TextView tvHeure = view.findViewById(R.id.textViewHeure);
        TextView tvDuree = view.findViewById(R.id.textViewDuree);
        TextView tvContenu = view.findViewById(R.id.textViewContenuPedagogique);

        MyAppViewModel myAppViewModel = new ViewModelProvider(requireActivity()).get(MyAppViewModel.class);
        myAppViewModel.setCurrentSeanceId(seanceId);
        Log.d("MYAPP:ARG:SEANCEF", "seanceId=" + seanceId);
        Log.d("MYAPP:ARG:SEANCEF", "seance=" + seance);

        myAppViewModel.getCurrentSeanceDTO().observe(getViewLifecycleOwner(), seanceObj -> {
            Log.d("MYAPP::SEANCEF", "myAppViewModel.getCurrentSeanceDTO()");
            Log.d("MYAPP::SEANCEF", "seanceObj=" + seanceObj);
            if (seanceObj.isEmpty()) return;
            tvMatiere.setText(seanceObj.get("matiere"));
            tvEnseignant.setText(seanceObj.get("enseignant"));
            Log.d("MYAPP::SEANCEF", "date=" + seanceObj.get("date"));
            LocalDate date = LocalDate.parse(seanceObj.get("date"), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            tvDate.setText(date.format(DateTimeFormatter.ofPattern("EEEE dd MMM yyyy", Locale.getDefault())));
            tvHeure.setText(seanceObj.get("heure_debut"));
            String duree = seanceObj.get("duree") + " heures";
            tvDuree.setText(duree);
            tvContenu.setText(seanceObj.get("contenu_pedagogique"));
            Log.d("MYAPP::SEANCEF", "END myAppViewModel.getCurrentSeanceDTO()");
        });

        view.findViewById(R.id.btnSeanceDelete).setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Suppression")
                    .setMessage("Êtes-vous sûr de vouloir supprimer cette seance")
                    .setNegativeButton("NON", (dialog, which) -> {

                    })
                    .setPositiveButton("OUI", (dialog, which) -> {
                        myAppViewModel.deleteSeance(seanceId);
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(),
                                    "Seance supprimée avec succès",
                                    Toast.LENGTH_LONG).show();
                            getNavController().popBackStack();
                        });
                    }).show();
        });

        view.findViewById(R.id.btnSeanceEdit).setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("selectedMatiereId", seance.getMatiereId());
            args.putInt("seanceId", seanceId);
            args.putBoolean("isEditing", true);
            getNavController().navigate(R.id.ajouterSeanceFragment, args);
        });
    }
}