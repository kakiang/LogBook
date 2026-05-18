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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MatiereListAdapter extends ListAdapter<MatiereListAdapter.MatiereWithStats, MatiereListAdapter.ViewHolder> {

    private Map<Integer, String> ueMap = new HashMap<>();

    public MatiereListAdapter() {
        super(DIFF_CALLBACK);
    }

    public record MatiereWithStats(
            Matiere ue,
            String volumeHoraireStat,
            int pourcentage) {}

    private static final DiffUtil.ItemCallback<MatiereListAdapter.MatiereWithStats> DIFF_CALLBACK =new DiffUtil.ItemCallback<MatiereListAdapter.MatiereWithStats>() {
        @Override
        public boolean areItemsTheSame(@NonNull MatiereListAdapter.MatiereWithStats oldItem, @NonNull MatiereListAdapter.MatiereWithStats newItem) {
            // Compare using the Matiere's identity (e.g., name or unique constraint)
            return oldItem.ue().getNom().equals(newItem.ue().getNom()) &&
                    oldItem.ue().getUeId() == newItem.ue().getUeId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull MatiereListAdapter.MatiereWithStats oldItem, @NonNull MatiereListAdapter.MatiereWithStats newItem) {
            // Check if any details or the bound UE Name itself changed
            return oldItem.equals(newItem) &&
                    oldItem.ue().getVolumeHoraire() == newItem.ue().getVolumeHoraire() &&
                    oldItem.ue().getEnseignant().equals(newItem.ue().getEnseignant());
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewVhStats;
        private final TextView textViewMatiereNom;
        private final TextView textViewEnseignant;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewVhStats = itemView.findViewById(R.id.textViewVhStats);
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
        MatiereListAdapter.MatiereWithStats m = getItem(position);
        Matiere matiere = m.ue();
        holder.textViewMatiereNom.setText(matiere.getNom());
        holder.textViewEnseignant.setText(matiere.getEnseignant());
        String stat = m.volumeHoraireStat() + " : " + m.pourcentage()+"%";
        holder.textViewVhStats.setText(stat);
    }

    public Map<Integer, String> getUeMap() {
        return ueMap;
    }

    public void setUeMap(Map<Integer, String> ueMap) {
        this.ueMap = ueMap;
    }
}
