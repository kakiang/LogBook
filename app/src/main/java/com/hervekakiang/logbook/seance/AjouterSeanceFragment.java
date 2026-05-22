package com.hervekakiang.logbook.seance;

import android.app.Dialog;
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
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.hervekakiang.logbook.R;
import com.hervekakiang.logbook.matiere.Matiere;
import com.hervekakiang.logbook.matiere.MatiereViewModel;
import com.hervekakiang.logbook.ue.UEViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AjouterSeanceFragment extends BottomSheetDialogFragment {
    private UEViewModel ueViewModel;

    private AutoCompleteTextView autoCompleteMatiere;
    private TextInputEditText editDate, editHeure, editDuree, editContenu;
    private int selectedMatiereId = -1;
    private List<Matiere> matiereList = new ArrayList<>();

    private int originalSoftInputMode;

    public AjouterSeanceFragment() {
    }

    public AjouterSeanceFragment(int matiereId) {
        this.selectedMatiereId = matiereId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ajouter_seance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.seanceToolbar);
        autoCompleteMatiere = view.findViewById(R.id.autoCompleteMatiere);
        editDate = view.findViewById(R.id.editSeanceDate);
        editHeure = view.findViewById(R.id.editSeanceHeure);
        editDuree = view.findViewById(R.id.editSeanceDuree);
        editContenu = view.findViewById(R.id.editSeanceContenu);
        Button btnSave = view.findViewById(R.id.btnSaveSeance);

        ueViewModel = new ViewModelProvider(requireActivity()).get(UEViewModel.class);

        toolbar.setNavigationOnClickListener(v -> {
            dismiss();
        });

        setupPickers();
        loadMatieres();

        btnSave.setOnClickListener(v -> saveSeance());
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
            hasError = true;
        } else {
            editDate.setError(null);
        }
        if (TextUtils.isEmpty(heure)) {
            editHeure.setError("Veuillez entrer une heure");
            hasError = true;
        } else {
            editHeure.setError(null);
        }
        if (TextUtils.isEmpty(duree)){
            editDuree.setError("Veuillez entrer une durée");
            hasError = true;
        } else {
            editDuree.setError(null);
        }
        if (TextUtils.isEmpty(contenu)) {
            editContenu.setError("Veuillez entrer un contenu");
            hasError = true;
        } else {
            editContenu.setError(null);
        }
        if (hasError) return;

        Seance seance = new Seance(selectedMatiereId, date, heure, Integer.parseInt(duree), contenu);

        ueViewModel.addSeance(seance, () -> {
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(getActivity(), "Séance ajoutée avec succès", Toast.LENGTH_SHORT).show();
                dismiss();
            });
        });
    }

    private void loadMatieres() {
        ueViewModel.getListMatieres().observe(getViewLifecycleOwner(), matieres -> {
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
//                        autoCompleteMatiere.setListSelection(selectedMatiereId);
                        break;
                    }
                }
            }

            autoCompleteMatiere.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selectedMatiereId = matiereList.get(position).getId();
                    autoCompleteMatiere.setError(null);
                }
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

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog instanceof BottomSheetDialog) {
            View bottomSheet = ((BottomSheetDialog) dialog).findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        }
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