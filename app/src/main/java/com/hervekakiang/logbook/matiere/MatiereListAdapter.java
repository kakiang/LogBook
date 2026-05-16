package com.hervekakiang.logbook.matiere;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.hervekakiang.logbook.R;
import com.hervekakiang.logbook.ue.UE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MatiereListAdapter extends ListAdapter<Pair<Matiere, String>, MatiereListAdapter.ViewHolder> {

    private Map<Integer, String> ueMap = new HashMap<>();

    public MatiereListAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Pair<Matiere, String>> DIFF_CALLBACK =new DiffUtil.ItemCallback<Pair<Matiere, String>>() {
        @Override
        public boolean areItemsTheSame(@NonNull Pair<Matiere, String> oldItem, @NonNull Pair<Matiere, String> newItem) {
            // Compare using the Matiere's identity (e.g., name or unique constraint)
            return oldItem.first.getNom().equals(newItem.first.getNom()) &&
                    oldItem.first.getUeId() == newItem.first.getUeId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Pair<Matiere, String> oldItem, @NonNull Pair<Matiere, String> newItem) {
            // Check if any details or the bound UE Name itself changed
            return oldItem.second.equals(newItem.second) &&
                    oldItem.first.getVolumeHoraire() == newItem.first.getVolumeHoraire() &&
                    oldItem.first.getEnseignant().equals(newItem.first.getEnseignant());
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewUeNom;
        private final TextView textViewVolumeHoraire;
        private final TextView textViewMatiereNom;
        private final TextView textViewEnseignant;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewUeNom = itemView.findViewById(R.id.tvUeNom);
            textViewVolumeHoraire = itemView.findViewById(R.id.tvVolumeHoraire);
            textViewMatiereNom = itemView.findViewById(R.id.tvMatiereNom);
            textViewEnseignant = itemView.findViewById(R.id.tvEnseignant);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.matiere_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pair<Matiere, String> pair = getItem(position);
        Matiere matiere = pair.first;
        String ueNom = pair.second;
        holder.textViewUeNom.setText(ueNom != null ? ueNom.toUpperCase() : "UE NON TROUVÉE");
        holder.textViewVolumeHoraire.setText(String.format(Locale.getDefault(), "%dh", matiere.getVolumeHoraire()));
        holder.textViewMatiereNom.setText(matiere.getNom());
        holder.textViewEnseignant.setText(matiere.getEnseignant());
    }

    public Map<Integer, String> getUeMap() {
        return ueMap;
    }

    public void setUeMap(Map<Integer, String> ueMap) {
        this.ueMap = ueMap;
    }
}
