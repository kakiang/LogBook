package com.hervekakiang.logbook.ue;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.hervekakiang.logbook.MyAppViewModel;
import com.hervekakiang.logbook.R;

public class AddUeFragment extends Fragment {

    private MyAppViewModel viewModel;
    private TextInputEditText editTextUeCode;
    private TextInputEditText editTextUeNom;

    private NavController navController;

    private int editingUeId = -1;
    private boolean isEditing = false;
    private UE editingUe;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_ue, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        LinearLayout layout = view.findViewById(R.id.layoutAddUe);
        layout.setMinimumHeight(Resources.getSystem().getDisplayMetrics().heightPixels);
        MaterialToolbar toolbar = view.findViewById(R.id.ueToolbar);

        if (getArguments() != null) {
            editingUeId = getArguments().getInt("ueId");
            isEditing = getArguments().getBoolean("isEditing");
        }
        toolbar.setNavigationOnClickListener(v -> navController.popBackStack());
        toolbar.setOnMenuItemClickListener(item -> {
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
            toolbar.setTitle("Modifier l'UE");

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
                    navController.popBackStack();
                });
            });
        } else {
            viewModel.addUE(new UE(code, nom), () -> {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "UE " + nom + "ajouté avec succès", Toast.LENGTH_SHORT).show();
                    navController.popBackStack();
                });
            });
        }

    }
}