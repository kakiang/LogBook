package com.hervekakiang.logbook.matiere;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hervekakiang.logbook.R;
import com.hervekakiang.logbook.ue.UE;
import com.hervekakiang.logbook.ue.UEViewModel;

import java.util.ArrayList;
import java.util.List;

public class AjouterMatiereFragment extends Fragment {

    private UEViewModel ueViewModel;

    private TextInputLayout textInputLayoutMatiereNom;
    private TextInputLayout textInputLayoutMatiereEnseignant;
    private TextInputLayout textInputLayoutMatiereVolumeHoraire;
    private TextInputLayout textInputLayoutUE;
    private TextInputEditText editTextMatiereNom;
    private TextInputEditText editTextEnseignant;
    private TextInputEditText editTextVolumeHoraire;

    private NavController navController;

    private List<UE> ueList = new ArrayList<>();
    private int selectedUeId = -1;
    private int editMatiereId = -1;
    private boolean isEditing = false;
    private Matiere matiereToEdit;

    public AjouterMatiereFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ajouter_matiere, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        MaterialToolbar toolbar = view.findViewById(R.id.matiereToolbar);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);

        AutoCompleteTextView autoCompleteUE = view.findViewById(R.id.autoCompleteUE);
        editTextMatiereNom = view.findViewById(R.id.editMatiereNom);
        editTextEnseignant = view.findViewById(R.id.editEnseignant);
        editTextVolumeHoraire = view.findViewById(R.id.editVolumeHoraire);

        textInputLayoutMatiereNom = view.findViewById(R.id.textInputLayoutMatiereNom);
        textInputLayoutMatiereEnseignant = view.findViewById(R.id.extInputLayoutMatiereEnseignant);
        textInputLayoutMatiereVolumeHoraire = view.findViewById(R.id.extInputLayoutMatiereVolumeHoraire);
        textInputLayoutUE = view.findViewById(R.id.textInputLayoutUE);
        Button btnSave = view.findViewById(R.id.btnSaveMatiere);

        btnSave.setOnClickListener(v -> saveMatiere());
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_save) {
                saveMatiere();
                return true;
            }
            return false;
        });

        ueViewModel = new ViewModelProvider(requireActivity()).get(UEViewModel.class);

        if (getArguments() != null) {
            selectedUeId = getArguments().getInt("selectedUeId");
            editMatiereId = getArguments().getInt("matiereId");
            isEditing = getArguments().getBoolean("isEditing");
        }

        Log.d("MYAPP::AjoutMatiere", "selectedUeId:" + selectedUeId);
        Log.d("MYAPP::AjoutMatiere", "editMatiereId:" + editMatiereId);
        Log.d("MYAPP::AjoutMatiere", "isEditing:" + isEditing);

        if (isEditing && editMatiereId != -1) {
            toolbar.setTitle("Modifier la matière");

            ueViewModel.getListMatieres().observe(getViewLifecycleOwner(), matieres -> {
                if (matieres == null) return;
                for (Matiere m : matieres) {
                    if (m.getId() == editMatiereId) {
                        Log.d("AJMatiere MYAPP", "m.getId() == editMatiereId");
                        Log.d("AJMatiere MYAPP", m.toString());

                        matiereToEdit = m;
                        selectedUeId = m.getUeId();

                        editTextMatiereNom.setText(m.getNom());
                        editTextEnseignant.setText(m.getEnseignant());
                        editTextVolumeHoraire.setText(String.valueOf(m.getVolumeHoraire()));
                        break;
                    }
                }
            });
        }
        
        ueViewModel.getListUEs().observe(getViewLifecycleOwner(), ues -> {
            this.ueList = ues;
            List<String> ueNoms = new ArrayList<>();
            for (UE ue : ueList) {
                ueNoms.add(ue.getNom());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, ueNoms);
            autoCompleteUE.setAdapter(adapter);

            if (selectedUeId != -1) {
                for (UE ue : ueList) {
                    if (ue.getId() == selectedUeId) {
                        autoCompleteUE.setText(ue.getNom(), false);
                        break;
                    }
                }
            }
        });

        autoCompleteUE.setOnItemClickListener((parent, view1, position, id) -> {
            selectedUeId = ueList.get(position).getId();
            autoCompleteUE.setError(null);
        });


    }

    private void saveMatiere() {
        String nom = !TextUtils.isEmpty(editTextMatiereNom.getText()) ? editTextMatiereNom.getText().toString() : null;
        String enseignant = !TextUtils.isEmpty(editTextEnseignant.getText()) ? editTextEnseignant.getText().toString() : null;
        String volumeHoraire = !TextUtils.isEmpty(editTextVolumeHoraire.getText()) ? editTextVolumeHoraire.getText().toString() : null;

        boolean hasError = false;
        if (selectedUeId == -1) {
            textInputLayoutUE.setError("Veuillez sélectionner une UE");
            hasError = true;
        } else {
            textInputLayoutUE.setError(null);
        }
        if (TextUtils.isEmpty(nom)) {
            textInputLayoutMatiereNom.setError("Veuillez entrer un nom");
            hasError = true;
        } else {
            textInputLayoutMatiereNom.setError(null);
        }
        if (TextUtils.isEmpty(enseignant)) {
            textInputLayoutMatiereEnseignant.setError("Veuillez entrer un enseignant");
            hasError = true;
        } else {
            textInputLayoutMatiereEnseignant.setError(null);
        }
        if (TextUtils.isEmpty(volumeHoraire)) {
            textInputLayoutMatiereVolumeHoraire.setError("Veuillez entrer un volume horaire");
            hasError = true;
        } else {
            textInputLayoutMatiereVolumeHoraire.setError(null);
        }
        if (hasError) return;

        if (isEditing && matiereToEdit != null) {
            matiereToEdit.setNom(nom);
            matiereToEdit.setEnseignant(enseignant);
            matiereToEdit.setVolumeHoraire(Integer.parseInt(volumeHoraire));
            ueViewModel.updateMatiere(matiereToEdit, () -> {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Matière mise à jour", Toast.LENGTH_SHORT).show();
                    navController.navigateUp();
                });
            });
        } else {
            Matiere newMatiere = new Matiere(selectedUeId, nom, enseignant, Integer.parseInt(volumeHoraire));
            ueViewModel.addMatiere(newMatiere, selectedUeId, () -> {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Matière " + nom + " ajoutée", Toast.LENGTH_SHORT).show();
                    navController.popBackStack();
                });
            });
        }
    }
}