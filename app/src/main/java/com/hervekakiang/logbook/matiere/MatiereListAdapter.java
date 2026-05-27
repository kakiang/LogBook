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

public class MatiereListAdapter extends ListAdapter<MatiereListAdapter.MatiereDTO, MatiereListAdapter.ViewHolder> {

    private OnItemClickListener<MatiereDTO> onItemClickListener;

    public MatiereListAdapter() {
        super(DIFF_CALLBACK);
    }

    public record MatiereDTO(
            Matiere matiere,
            String volumeHoraireStat,
            int pourcentage) implements Serializable {
    }

    private static final DiffUtil.ItemCallback<MatiereDTO> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull MatiereDTO oldItem, @NonNull MatiereDTO newItem) {
            return oldItem.matiere().getNom().equals(newItem.matiere().getNom()) &&
                    oldItem.matiere().getUeId() == newItem.matiere().getUeId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull MatiereDTO oldItem, @NonNull MatiereDTO newItem) {
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
        MatiereDTO matiereDTO = getItem(position);
        Matiere matiere = matiereDTO.matiere();
        holder.textViewMatiereNom.setText(matiere.getNom());
        holder.textViewEnseignant.setText(matiere.getEnseignant());
        String stat = matiereDTO.volumeHoraireStat() + " | " + matiereDTO.pourcentage() + "%";
        holder.textViewVhStats.setText(stat);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(matiereDTO);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemLongClick(matiereDTO);
            }
            return false;
        });
    }

    public void setOnItemClickListener(OnItemClickListener<MatiereDTO> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
