package com.hervekakiang.logbook.ue;

import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.hervekakiang.logbook.R;

public class AddUeFragment extends BottomSheetDialogFragment {

    BottomSheetBehavior<View> bottomSheetBehavior;
    private int originalSoftInputMode;

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

        bottomSheetBehavior = BottomSheetBehavior.from((View) view.getParent());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        NestedScrollView nestedScrollView = view.findViewById(R.id.scrollView_add_ue);
        nestedScrollView.setMinimumHeight(Resources.getSystem().getDisplayMetrics().heightPixels);

        UEViewModel viewModel = new ViewModelProvider(requireActivity()).get(UEViewModel.class);
        TextInputEditText editUeCode = view.findViewById(R.id.editUeCode);
        TextInputEditText editUeNom = view.findViewById(R.id.editUeNom);
        Button btnSaveUe = view.findViewById(R.id.btnSaveUe);

        btnSaveUe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = editUeCode.getText() != null ? editUeCode.getText().toString() : null;
                String nom = editUeNom.getText() != null ? editUeNom.getText().toString() : null;
                if (code == null || nom == null) {
                    Toast.makeText(getActivity(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                    return;
                }
                viewModel.addUE(new UE(code, nom));
                Toast.makeText(getActivity(), "UE " + nom + "ajouté avec succès", Toast.LENGTH_SHORT).show();
                dismiss();

            }
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