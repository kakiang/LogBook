package com.hervekakiang.logbook.matiere;

import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
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

    private UEViewModel ueViewModel;

    private TextInputLayout textInputLayoutMatiereNom;
    private TextInputLayout textInputLayoutMatiereEnseignant;
    private TextInputLayout textInputLayoutMatiereVolumeHoraire;
    private TextInputLayout textInputLayoutUE;
    private TextInputEditText editMatiereNom;
    private TextInputEditText editEnseignant;
    private TextInputEditText editVolumeHoraire;

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
        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from((View) view.getParent());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setSkipCollapsed(true);

        LinearLayout layout = view.findViewById(R.id.layoutAddMatiere);
        layout.setMinimumHeight(Resources.getSystem().getDisplayMetrics().heightPixels);
        MaterialToolbar toolbar = view.findViewById(R.id.matiereToolbar);
        toolbar.setNavigationOnClickListener(v -> {
            dismiss();
        });
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_save) {
                saveMatiere();
                return true;
            }
            return false;
        });

        AutoCompleteTextView autoCompleteUE = view.findViewById(R.id.autoCompleteUE);
        editMatiereNom = view.findViewById(R.id.editMatiereNom);
        editEnseignant = view.findViewById(R.id.editEnseignant);
        editVolumeHoraire = view.findViewById(R.id.editVolumeHoraire);

        textInputLayoutMatiereNom = view.findViewById(R.id.textInputLayoutMatiereNom);
        textInputLayoutMatiereEnseignant = view.findViewById(R.id.extInputLayoutMatiereEnseignant);
        textInputLayoutMatiereVolumeHoraire = view.findViewById(R.id.extInputLayoutMatiereVolumeHoraire);
        textInputLayoutUE = view.findViewById(R.id.textInputLayoutUE);

        ueViewModel = new ViewModelProvider(requireActivity()).get(UEViewModel.class);
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

        autoCompleteUE.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedUeId = ueList.get(position).getId();
                autoCompleteUE.setError(null);
            }
        });

        view.findViewById(R.id.btnSaveMatiere).setOnClickListener(v -> {
            saveMatiere();
        });
    }

    private void saveMatiere() {
        String nom = !TextUtils.isEmpty(editMatiereNom.getText()) ? editMatiereNom.getText().toString() : null;
        String enseignant = !TextUtils.isEmpty(editEnseignant.getText()) ? editEnseignant.getText().toString() : null;
        String volumeHoraire = !TextUtils.isEmpty(editVolumeHoraire.getText()) ? editVolumeHoraire.getText().toString() : null;

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

        Matiere newMatiere = new Matiere(selectedUeId, nom, enseignant, Integer.parseInt(volumeHoraire));
        ueViewModel.addMatiere(newMatiere, selectedUeId, () -> {
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(getActivity(), "Matière " + nom + " ajoutée", Toast.LENGTH_SHORT).show();
                dismiss();
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