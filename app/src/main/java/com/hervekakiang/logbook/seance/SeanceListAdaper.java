package com.hervekakiang.logbook.seance;

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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class SeanceListAdaper extends ListAdapter<Seance, SeanceListAdaper.SeanceViewHolder> {
    private OnItemClickListener<Seance> onItemClickListener;

    public static class SeanceViewHolder extends RecyclerView.ViewHolder {
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

    public record SeanceDTO(Seance seance, String matiereNom) implements Serializable {}


    public SeanceListAdaper() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Seance> DIFF_CALLBACK = new DiffUtil.ItemCallback<Seance>() {
        @Override
        public boolean areItemsTheSame(@NonNull Seance oldItem, @NonNull Seance newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Seance oldItem, @NonNull Seance newItem) {
            return oldItem.getDate().equals(newItem.getDate()) &&
                    oldItem.getHeureDebut().equals(newItem.getHeureDebut()) &&
                    oldItem.getDuree() == newItem.getDuree() &&
                    oldItem.getContenuPedagogique().equals(newItem.getContenuPedagogique());
        }
    };

    @NonNull
    @Override
    public SeanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_seance_row, parent, false);
        return new SeanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeanceViewHolder holder, int position) {
        Seance seance = getItem(position);
        LocalDate date = LocalDate.parse(seance.getDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String periode = date.format(DateTimeFormatter.ofPattern("E dd MMM", Locale.getDefault()))+ " " + seance.getHeureDebut();
        holder.textViewSeanceDate.setText(periode);
        holder.textViewSeanceDuree.setText(String.format(Locale.getDefault(), "%dh", seance.getDuree()));
        holder.textViewSeanceContenu.setText(seance.getContenuPedagogique());

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(seance);
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener<Seance> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
