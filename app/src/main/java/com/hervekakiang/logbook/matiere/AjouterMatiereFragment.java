package com.hervekakiang.logbook.matiere;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hervekakiang.logbook.BaseFragment;
import com.hervekakiang.logbook.MyAppViewModel;
import com.hervekakiang.logbook.R;
import com.hervekakiang.logbook.ue.UE;

import java.util.ArrayList;
import java.util.List;

public class AjouterMatiereFragment extends BaseFragment {

    private MyAppViewModel myAppViewModel;

    private TextInputLayout textInputLayoutMatiereNom;
    private TextInputLayout textInputLayoutMatiereEnseignant;
    private TextInputLayout textInputLayoutMatiereVolumeHoraire;
    private TextInputLayout textInputLayoutUE;
    private TextInputEditText editTextMatiereNom;
    private TextInputEditText editTextEnseignant;
    private TextInputEditText editTextVolumeHoraire;

    private List<UE> ueList = new ArrayList<>();
    private int selectedUeId = -1;
    private int editMatiereId = -1;
    private boolean isEditing = false;
    private Matiere matiereToEdit;

    public AjouterMatiereFragment() {
        super(R.layout.fragment_ajouter_matiere);
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_ajouter_matiere, container, false);
//    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        getToolbar().setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_save) {
                saveMatiere();
                return true;
            }
            return false;
        });

        myAppViewModel = new ViewModelProvider(requireActivity()).get(MyAppViewModel.class);

        if (getArguments() != null) {
            selectedUeId = getArguments().getInt("selectedUeId");
            editMatiereId = getArguments().getInt("matiereId");
            isEditing = getArguments().getBoolean("isEditing");
        }

        Log.d("MYAPP::AjoutMatiere", "selectedUeId:" + selectedUeId);
        Log.d("MYAPP::AjoutMatiere", "editMatiereId:" + editMatiereId);
        Log.d("MYAPP::AjoutMatiere", "isEditing:" + isEditing);

        if (isEditing && editMatiereId != -1) {
            getToolbar().setTitle("Modifier la matière");

            myAppViewModel.getListMatieres().observe(getViewLifecycleOwner(), matieres -> {
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

        myAppViewModel.getListUEs().observe(getViewLifecycleOwner(), ues -> {
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
            matiereToEdit.setUeId(selectedUeId);
            matiereToEdit.setNom(nom);
            matiereToEdit.setEnseignant(enseignant);
            matiereToEdit.setVolumeHoraire(Integer.parseInt(volumeHoraire));
            myAppViewModel.updateMatiere(matiereToEdit, () -> {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Matière mise à jour", Toast.LENGTH_SHORT).show();
                    getNavController().navigateUp();
                });
            });
        } else {
            Matiere newMatiere = new Matiere(selectedUeId, nom, enseignant, Integer.parseInt(volumeHoraire));
            myAppViewModel.addMatiere(newMatiere, selectedUeId, () -> {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Matière " + nom + " ajoutée", Toast.LENGTH_SHORT).show();
                    getNavController().popBackStack();
                });
            });
        }
    }
}