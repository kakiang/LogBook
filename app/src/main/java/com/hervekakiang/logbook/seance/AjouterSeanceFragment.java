package com.hervekakiang.logbook.seance;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.hervekakiang.logbook.BaseFragment;
import com.hervekakiang.logbook.MyAppViewModel;
import com.hervekakiang.logbook.R;
import com.hervekakiang.logbook.matiere.Matiere;
import com.hervekakiang.logbook.matiere.MatiereViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AjouterSeanceFragment extends BaseFragment {
    private MyAppViewModel myAppViewModel;

    private AutoCompleteTextView autoCompleteMatiere;
    private TextInputEditText editDate, editHeure, editDuree, editContenu;
    private TextInputLayout editDateLayout, editHeureLayout, editDureeLayout, editContenuLayout;
    private int selectedMatiereId = -1;
    private boolean isEditing = false;
    private int seanceIdToEdit = -1;
    private Seance seanceToEdit;
    private List<Matiere> matiereList = new ArrayList<>();


    public AjouterSeanceFragment() {
        super(R.layout.fragment_ajouter_seance);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            selectedMatiereId = getArguments().getInt("selectedMatiereId");
            isEditing = getArguments().getBoolean("isEditing");
            seanceIdToEdit = getArguments().getInt("seanceId");
        }
        Log.d("MYAPP:==ADD SEANCE=======", "====================================");
        Log.d("MYAPP:selectedMatiereId=", selectedMatiereId+"");
        Log.d("MYAPP:isEditing=", isEditing+"");
        Log.d("MYAPP:seanceIdToEdit=", seanceIdToEdit+"");


        autoCompleteMatiere = view.findViewById(R.id.autoCompleteMatiere);
        editDate = view.findViewById(R.id.editSeanceDate);
        editHeure = view.findViewById(R.id.editSeanceHeure);
        editDuree = view.findViewById(R.id.editSeanceDuree);
        editContenu = view.findViewById(R.id.editSeanceContenu);

        editDureeLayout = view.findViewById(R.id.layoutSeanceDuree);
        editDureeLayout.setErrorEnabled(true);
        editDateLayout = view.findViewById(R.id.layoutSeanceDate);
        editDateLayout.setErrorEnabled(true);
        editHeureLayout = view.findViewById(R.id.layoutSeanceHeure);
        editHeureLayout.setErrorEnabled(true);
        editContenuLayout = view.findViewById(R.id.layoutSeanceContenu);
        editContenuLayout.setErrorEnabled(true);


        myAppViewModel = new ViewModelProvider(requireActivity()).get(MyAppViewModel.class);

        if (isEditing && seanceIdToEdit != -1) {
            getToolbar().setTitle("Modifier la seance");
            myAppViewModel.getListSeances().observe(getViewLifecycleOwner(), seances -> {
                if (seances == null) {
                    Log.d("MYAPP:==getListSeanceForCurrentMatiere==", "null");
                    return;
                }
                for (Seance s : seances) {
                    if (s.getId() == seanceIdToEdit) {
                        seanceToEdit = s;
                        editDate.setText(s.getDate());
                        editHeure.setText(s.getHeureDebut());
                        editDuree.setText(String.valueOf(s.getDuree()));
                        editContenu.setText(s.getContenuPedagogique());
                        break;
                    }
                }
            });
        }

        getToolbar().setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_save) {
                saveSeance();
                return true;
            }
            return false;
        });

        setupPickers();
        loadMatieres();

        view.findViewById(R.id.btnSaveSeance).setOnClickListener(v -> saveSeance());
    }

    private void saveSeance() {
        String date = !TextUtils.isEmpty(editDate.getText()) ? editDate.getText().toString() : null;
        String heure = !TextUtils.isEmpty(editHeure.getText()) ? editHeure.getText().toString() : null;
        String duree = !TextUtils.isEmpty(editDuree.getText()) ? editDuree.getText().toString() : null;
        String contenu = !TextUtils.isEmpty(editContenu.getText()) ? editContenu.getText().toString() : null;
        boolean hasError = false;

        if (selectedMatiereId == -1) {
            autoCompleteMatiere.setError("Veuillez sélectionner une matière");
            hasError = true;
        } else {
            autoCompleteMatiere.setError(null);
        }
        if (TextUtils.isEmpty(date)) {
            editDate.setError("Veuillez entrer une date");
            editDateLayout.setError("Veuillez entrer une date");
            hasError = true;
        } else {
            editDateLayout.setError(null);
            editDate.setError(null);
        }
        if (TextUtils.isEmpty(heure)) {
            editHeure.setError("Veuillez entrer une heure");
            editHeureLayout.setError("Veuillez entrer une heure");
            hasError = true;
        } else {
            editHeure.setError(null);
            editHeureLayout.setError(null);
        }
        if (TextUtils.isEmpty(duree)) {
            editDuree.setError("Veuillez entrer une durée");
            editDureeLayout.setError("Veuillez entrer une durée");
            hasError = true;
        } else {
            editDuree.setError(null);
            editDureeLayout.setError(null);
        }
        if (TextUtils.isEmpty(contenu)) {
            editContenu.setError("Veuillez entrer un contenu");
            editContenuLayout.setError("Veuillez entrer un contenu");
            hasError = true;
        } else {
            editContenu.setError(null);
            editContenuLayout.setError(null);
        }
        if (hasError) return;

        MatiereViewModel matiereViewModel = new ViewModelProvider(requireActivity()).get(MatiereViewModel.class);
        matiereViewModel.getMatiereById(selectedMatiereId).observe(getViewLifecycleOwner(), matiere -> {
            if (matiere == null) return;
            int vhDispense = matiereViewModel.getTotalVhDispenseByMatiere(selectedMatiereId);
            if (vhDispense + Integer.parseInt(duree) > matiere.getVolumeHoraire()) {
                Toast.makeText(getActivity(),
                        "La durée de la séance dépasse le volume horaire de la matière",
                        Toast.LENGTH_SHORT).show();
                int maxDuree = matiere.getVolumeHoraire() - vhDispense;
                editDuree.setError("Durée maximale " + maxDuree + " heures");
                editDureeLayout.setError("Durée maximale " + maxDuree + " heures");
                return;
            } else {
                editDuree.setError(null);
                editDureeLayout.setError(null);
            }

            if (isEditing && seanceToEdit != null) {
                seanceToEdit.setDate(date);
                seanceToEdit.setHeureDebut(heure);
                seanceToEdit.setDuree(Integer.parseInt(duree));
                seanceToEdit.setContenuPedagogique(contenu);

                myAppViewModel.updateSeance(seanceToEdit, () -> requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Séance mise à jour", Toast.LENGTH_SHORT).show();
                    getNavController().navigate(R.id.seanceListFragment);
                }));
            } else {
                Seance seance = new Seance(selectedMatiereId, date, heure, Integer.parseInt(duree), contenu);
                myAppViewModel.addSeance(seance, () -> {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(), "Séance ajoutée avec succès", Toast.LENGTH_SHORT).show();
                        getNavController().navigate(R.id.seanceListFragment);
                    });
                });
            }
        });


    }

    private void loadMatieres() {
        myAppViewModel.getListMatieres().observe(getViewLifecycleOwner(), matieres -> {
            this.matiereList = matieres;
            List<String> matiereNoms = new ArrayList<>();
            for (Matiere matiere : matiereList) {
                matiereNoms.add(matiere.getNom());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, matiereNoms);
            autoCompleteMatiere.setAdapter(adapter);
            if (selectedMatiereId != -1) {
                for (Matiere m : matiereList) {
                    if (m.getId() == selectedMatiereId) {
                        autoCompleteMatiere.setText(m.getNom(), false);
                        break;
                    }
                }
            }

            autoCompleteMatiere.setOnItemClickListener((parent, view, position, id) -> {
                selectedMatiereId = matiereList.get(position).getId();
                autoCompleteMatiere.setError(null);
            });
        });

    }

    private void setupPickers() {
        editDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Choisir la date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(selection);
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                editDate.setText(format.format(calendar.getTime()));
            });
            datePicker.show(getChildFragmentManager(), "DATE_PICKER"); // Use getChildFragmentManager inside fragments
        });

        editHeure.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(8)
                    .setMinute(30)
                    .setTitleText("Heure de début")
                    .build();

            timePicker.addOnPositiveButtonClickListener(view -> {
                String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", timePicker.getHour(), timePicker.getMinute());
                editHeure.setText(formattedTime);
            });
            timePicker.show(getChildFragmentManager(), "TIME_PICKER");
        });
    }
}