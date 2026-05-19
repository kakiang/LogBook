package com.hervekakiang.logbook.seance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.hervekakiang.logbook.OnItemClickListener;
import com.hervekakiang.logbook.R;

import java.util.Locale;

public class SeanceListGroupAdaper extends ListAdapter<SeanceListItem, RecyclerView.ViewHolder> {
    private OnItemClickListener<Seance> onItemClickListener;

    public SeanceListGroupAdaper() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<SeanceListItem> DIFF_CALLBACK =new DiffUtil.ItemCallback<SeanceListItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull SeanceListItem oldItem, @NonNull SeanceListItem newItem) {
            // If types are different, they aren't the same item
            if (oldItem.getType() != newItem.getType()) return false;

            // If it's a header, check by Matiere ID mapping
            if (oldItem.getType() == SeanceListItem.TYPE_MATIERE) {
                return oldItem.getMatiereId() == newItem.getMatiereId();
            }

            // If it's a session card, check by its database primary key ID
            return oldItem.getSeance().getId() == newItem.getSeance().getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull SeanceListItem oldItem, @NonNull SeanceListItem newItem) {
            if (oldItem.getType() != newItem.getType()) return false;

            if (oldItem.getType() == SeanceListItem.TYPE_MATIERE) {
                return oldItem.getMatiereNom().equals(newItem.getMatiereNom()) &&
                        oldItem.isExpanded() == newItem.isExpanded();
            }

            // Check all content values to see if an item requires a redraw/rebind
            Seance oldSeance = oldItem.getSeance();
            Seance newSeance = newItem.getSeance();
            return oldSeance.getDate().equals(newSeance.getDate()) &&
                    oldSeance.getHeureDebut().equals(newSeance.getHeureDebut()) &&
                    oldSeance.getDuree() == newSeance.getDuree() &&
                    oldSeance.getContenuPedagogique().equals(newSeance.getContenuPedagogique());
        }
    };

    static class SeanceViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewSeanceDate;
        private final TextView textViewSeanceDuree;
        private final TextView textViewSeanceContenu;

        public SeanceViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSeanceDate = itemView.findViewById(R.id.tvSeanceDate);
            textViewSeanceDuree = itemView.findViewById(R.id.tvSeanceDuree);
            textViewSeanceContenu = itemView.findViewById(R.id.tvSeanceContenu);
        }
    }

    static class MatiereViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewHeaderMatiere;
        private final ImageView imageViewExpand;
        public MatiereViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewHeaderMatiere = itemView.findViewById(R.id.tvHeaderMatiere);
            imageViewExpand = itemView.findViewById(R.id.imageViewExpand);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == SeanceListItem.TYPE_MATIERE) {
            View view = inflater.inflate(R.layout.item_seance_header, parent, false);
            return new MatiereViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_seance_row, parent, false);
            return new SeanceViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SeanceListItem item = getItem(position);
        if(holder instanceof MatiereViewHolder matiereViewHolder) {
            matiereViewHolder.textViewHeaderMatiere.setText(item.getMatiereNom().toUpperCase());

            matiereViewHolder.imageViewExpand.setRotation(item.isExpanded() ? 180f : 0f);

        } else {
            SeanceViewHolder seanceViewHolder = (SeanceViewHolder) holder;
            Seance seance = item.getSeance();
            seanceViewHolder.textViewSeanceDate.setText(seance.getDate());
            seanceViewHolder.textViewSeanceDuree.setText(String.format(Locale.getDefault(),"%dh", seance.getDuree()));
            seanceViewHolder.textViewSeanceContenu.setText(seance.getContenuPedagogique());
        }

    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType();
    }
}
