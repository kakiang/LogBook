package com.hervekakiang.logbook.seance;

import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.hervekakiang.logbook.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AjouterSeanceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AjouterSeanceFragment extends BottomSheetDialogFragment {
    BottomSheetBehavior<View> bottomSheetBehavior;
    private int originalSoftInputMode;

    public AjouterSeanceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ajouter_seance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bottomSheetBehavior = BottomSheetBehavior.from((View) view.getParent());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setSkipCollapsed(true);

        LinearLayout layout = view.findViewById(R.id.layoutAddSeance);
        layout.setMinimumHeight(Resources.getSystem().getDisplayMetrics().heightPixels);

        MaterialToolbar toolbar = view.findViewById(R.id.seanceToolbar);
        toolbar.setNavigationOnClickListener(v -> {
            dismiss();
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