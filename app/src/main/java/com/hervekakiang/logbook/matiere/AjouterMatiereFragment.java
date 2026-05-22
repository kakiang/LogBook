package com.hervekakiang.logbook.matiere;

import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hervekakiang.logbook.R;
import com.hervekakiang.logbook.ue.UE;
import com.hervekakiang.logbook.ue.UEViewModel;

import java.util.ArrayList;
import java.util.List;

public class AjouterMatiereFragment extends BottomSheetDialogFragment {
    BottomSheetBehavior<View> bottomSheetBehavior;
    private int originalSoftInputMode;
    private List<UE> ueList = new ArrayList<>();
    private int selectedUeId = -1;

    public AjouterMatiereFragment() {
    }

    public AjouterMatiereFragment(int ueId) {
        this.selectedUeId = ueId;
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
        bottomSheetBehavior = BottomSheetBehavior.from((View) view.getParent());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setSkipCollapsed(true);

        LinearLayout layout = view.findViewById(R.id.layoutAddMatiere);
        layout.setMinimumHeight(Resources.getSystem().getDisplayMetrics().heightPixels);
        MaterialToolbar toolbar = view.findViewById(R.id.matiereToolbar);
        toolbar.setNavigationOnClickListener(v -> {
            dismiss();
        });

        AutoCompleteTextView autoCompleteUE = view.findViewById(R.id.autoCompleteUE);
        TextInputEditText editMatiereNom = view.findViewById(R.id.editMatiereNom);
        TextInputEditText editEnseignant = view.findViewById(R.id.editEnseignant);
        TextInputEditText editVolumeHoraire = view.findViewById(R.id.editVolumeHoraire);
        Button btnSaveMatiere = view.findViewById(R.id.btnSaveMatiere);

        TextInputLayout textInputLayoutMatiereNom = view.findViewById(R.id.textInputLayoutMatiereNom);
        TextInputLayout textInputLayoutMatiereEnseignant = view.findViewById(R.id.extInputLayoutMatiereEnseignant);
        TextInputLayout textInputLayoutMatiereVolumeHoraire = view.findViewById(R.id.extInputLayoutMatiereVolumeHoraire);
        TextInputLayout textInputLayoutUE = view.findViewById(R.id.textInputLayoutUE);


        UEViewModel ueViewModel = new ViewModelProvider(requireActivity()).get(UEViewModel.class);
        ueViewModel.getListUEs().observe(getViewLifecycleOwner(), ues -> {
            this.ueList = ues;
            List<String> ueNoms = new ArrayList<>();
            for (UE ue : ueList) {
                ueNoms.add(ue.getNom());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, ueNoms);
            autoCompleteUE.setAdapter(adapter);

            if (selectedUeId != -1) {
                for(UE ue : ueList) {
                    if (ue.getId() == selectedUeId) {
                        autoCompleteUE.setText(ue.getNom(), false);
//                        autoCompleteUE.setListSelection(selectedUeId);
                        break;
                    }
                }
            }
        });

        autoCompleteUE.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedUeId = ueList.get(position).getId();
                autoCompleteUE.setError(null);
            }
        });

        btnSaveMatiere.setOnClickListener(v -> {
            String nom = !TextUtils.isEmpty(editMatiereNom.getText()) ? editMatiereNom.getText().toString() : null;
            String enseignant = !TextUtils.isEmpty(editEnseignant.getText()) ? editEnseignant.getText().toString() : null;
            String volumeHoraire = !TextUtils.isEmpty(editVolumeHoraire.getText()) ? editVolumeHoraire.getText().toString() : null;
            if (selectedUeId == -1 || nom == null || enseignant == null || volumeHoraire == null) {
                if (selectedUeId == -1) {
                    textInputLayoutUE.setError("Veuillez sélectionner une UE");
                } else {
                    textInputLayoutUE.setError(null);
                }
                if (nom == null) {
                    textInputLayoutMatiereNom.setError("Veuillez entrer un nom");
                } else {
                    textInputLayoutMatiereNom.setError(null);
                }
                if (enseignant == null) {
                    textInputLayoutMatiereEnseignant.setError("Veuillez entrer un enseignant");
                } else {
                    textInputLayoutMatiereEnseignant.setError(null);
                }
                if (volumeHoraire == null) {
                    textInputLayoutMatiereVolumeHoraire.setError("Veuillez entrer un volume horaire");
                } else {
                    textInputLayoutMatiereVolumeHoraire.setError(null);
                }
                return;
            }
            Matiere newMatiere = new Matiere(selectedUeId, nom, enseignant, Integer.parseInt(volumeHoraire));
            ueViewModel.addMatiere(newMatiere, selectedUeId, () -> {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Matière " + nom + " ajoutée", Toast.LENGTH_SHORT).show();
                    dismiss();
                });
            });
        });

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // Store the activity current mode
        if (getActivity() != null && getActivity().getWindow() != null) {
            originalSoftInputMode = getActivity().getWindow().getAttributes().softInputMode;
            // CRITICAL: Set to PAN so activity does NOT resize
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
        return dialog;
    }

    @Override
    public void onDestroyView() {
        // Restore original mode when bottom sheet is dismissed
        if (getActivity() != null && getActivity().getWindow() != null) {
            getActivity().getWindow().setSoftInputMode(originalSoftInputMode);
        }
        super.onDestroyView();
    }
}