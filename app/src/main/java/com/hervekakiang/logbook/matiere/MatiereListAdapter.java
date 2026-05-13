package com.hervekakiang.logbook.matiere;

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
import java.util.List;

public class MatiereListAdapter extends ListAdapter<Matiere, MatiereListAdapter.ViewHolder> {// RecyclerView.Adapter<MatiereListAdapter.ViewHolder> {

    private List<Matiere> matieres = new ArrayList<>();

    public MatiereListAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Matiere> DIFF_CALLBACK = new DiffUtil.ItemCallback<Matiere>() {
        @Override
        public boolean areItemsTheSame(@NonNull Matiere oldItem, @NonNull Matiere newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Matiere oldItem, @NonNull Matiere newItem) {
            return oldItem.getNom().equals(newItem.getNom()) &&
                    oldItem.getUeId()==newItem.getUeId();
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
        Matiere matiere = matieres.get(position);
//        holder.textViewUeNom.setText(matiere.getUeNom());
        holder.textViewVolumeHoraire.setText(String.valueOf(matiere.getVolumeHoraire()));
        holder.textViewMatiereNom.setText(matiere.getNom());
        holder.textViewEnseignant.setText(matiere.getEnseignant());
    }

    @Override
    public int getItemCount() {
        return matieres.size();
    }

    public List<Matiere> getMatieres() {
        return matieres;
    }

    public void setMatieres(List<Matiere> matieres) {
        this.matieres = matieres;
    }
}
