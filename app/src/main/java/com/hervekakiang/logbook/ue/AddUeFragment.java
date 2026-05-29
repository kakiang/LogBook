package com.hervekakiang.logbook.ue;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.hervekakiang.logbook.BaseFragment;
import com.hervekakiang.logbook.MyAppViewModel;
import com.hervekakiang.logbook.R;

public class AddUeFragment extends BaseFragment {

    private MyAppViewModel viewModel;
    private TextInputEditText editTextUeCode;
    private TextInputEditText editTextUeNom;

    private int editingUeId = -1;
    private boolean isEditing = false;
    private UE editingUe;

    public AddUeFragment() {
        super(R.layout.fragment_add_ue);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            editingUeId = getArguments().getInt("ueId");
            isEditing = getArguments().getBoolean("isEditing");
        }
//        getToolbar().setNavigationOnClickListener(v -> getNavController().popBackStack());
        getToolbar().setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_save) {
                saveUE();
                return true;
            }
            return false;
        });

        viewModel = new ViewModelProvider(requireActivity()).get(MyAppViewModel.class);
        editTextUeCode = view.findViewById(R.id.editUeCode);
        editTextUeNom = view.findViewById(R.id.editUeNom);

        if (isEditing && editingUeId != -1) {
            getToolbar().setTitle("Modifier l'UE");

            viewModel.getListUEs().observe(getViewLifecycleOwner(), ues -> {
                for (UE ue : ues) {
                    if (ue.getId() == editingUeId) {
                        editingUe = ue;
                        editTextUeCode.setText(ue.getCode());
                        editTextUeNom.setText(ue.getNom());
                        break;
                    }
                }
            });
        }

        view.findViewById(R.id.btnSaveUe).setOnClickListener(v -> {
            saveUE();
        });
    }

    private void saveUE() {
        String code = editTextUeCode.getText() != null ? editTextUeCode.getText().toString() : null;
        String nom = editTextUeNom.getText() != null ? editTextUeNom.getText().toString() : null;
        if (code == null || nom == null) {
            Toast.makeText(getActivity(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditing && editingUe != null) {
            editingUe.setCode(code);
            editingUe.setNom(nom);

            viewModel.updateUE(editingUe, () -> {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "UE " + nom + "mise à jour", Toast.LENGTH_SHORT).show();
                    getNavController().popBackStack();
                });
            });
        } else {
            viewModel.addUE(new UE(code, nom), () -> {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "UE " + nom + "ajouté avec succès", Toast.LENGTH_SHORT).show();
                    getNavController().popBackStack();
                });
            });
        }

    }
}