package com.hervekakiang.logbook.matiere;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.hervekakiang.logbook.OnItemClickListener;
import com.hervekakiang.logbook.R;

import java.io.Serializable;

public class MatiereListAdapter extends ListAdapter<MatiereListAdapter.MatiereWithStats, MatiereListAdapter.ViewHolder> {

    private OnItemClickListener<MatiereWithStats> onItemClickListener;

    public MatiereListAdapter() {
        super(DIFF_CALLBACK);
    }

    public record MatiereWithStats(
            Matiere matiere,
            String volumeHoraireStat,
            int pourcentage) implements Serializable {}

    private static final DiffUtil.ItemCallback<MatiereListAdapter.MatiereWithStats> DIFF_CALLBACK =new DiffUtil.ItemCallback<MatiereListAdapter.MatiereWithStats>() {
        @Override
        public boolean areItemsTheSame(@NonNull MatiereListAdapter.MatiereWithStats oldItem, @NonNull MatiereListAdapter.MatiereWithStats newItem) {
            // Compare using the Matiere's identity (e.g., name or unique constraint)
            return oldItem.matiere().getNom().equals(newItem.matiere().getNom()) &&
                    oldItem.matiere().getUeId() == newItem.matiere().getUeId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull MatiereListAdapter.MatiereWithStats oldItem, @NonNull MatiereListAdapter.MatiereWithStats newItem) {
            // Check if any details or the bound UE Name itself changed
            return oldItem.equals(newItem) &&
                    oldItem.matiere().getVolumeHoraire() == newItem.matiere().getVolumeHoraire() &&
                    oldItem.matiere().getEnseignant().equals(newItem.matiere().getEnseignant());
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
        MatiereListAdapter.MatiereWithStats matiereWithStats = getItem(position);
        Matiere matiere = matiereWithStats.matiere();
        holder.textViewMatiereNom.setText(matiere.getNom());
        holder.textViewEnseignant.setText(matiere.getEnseignant());
        String stat = matiereWithStats.volumeHoraireStat() + " : " + matiereWithStats.pourcentage()+"%";
        holder.textViewVhStats.setText(stat);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(matiereWithStats);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemLongClick(matiereWithStats);
            }
            return false;
        });
    }

    public void setOnItemClickListener(OnItemClickListener<MatiereWithStats> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
