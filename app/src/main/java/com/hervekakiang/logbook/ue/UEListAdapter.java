package com.hervekakiang.logbook.ue;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.hervekakiang.logbook.OnItemClickListener;
import com.hervekakiang.logbook.R;

import java.io.Serializable;

public class UEListAdapter extends ListAdapter<UEListAdapter.UEDTO, UEListAdapter.ViewHolder> {

    private OnItemClickListener<UEDTO> onItemClickListener;

    public UEListAdapter() {
        super(DIFF_CALLBACK);
    }

    public record UEDTO(
            UE ue,
            String volumeHoraireStat,
            int pourcentage) implements Serializable {}

    private static final DiffUtil.ItemCallback<UEDTO> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull UEDTO oldItem, @NonNull UEDTO newItem) {
            return oldItem.ue.getId() == newItem.ue.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull UEDTO oldItem, @NonNull UEDTO newItem) {
            return oldItem.equals(newItem);
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewCode;
        private final TextView textViewNom;
        private final ProgressBar progressBar;
        private final TextView tvChartPercentage;
        private final TextView textViewVolumehoraireStat;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewCode = itemView.findViewById(R.id.textViewCode);
            textViewNom = itemView.findViewById(R.id.textViewNom);
            progressBar = itemView.findViewById(R.id.chartProgress);
            tvChartPercentage = itemView.findViewById(R.id.tvChartPercentage);
            textViewVolumehoraireStat = itemView.findViewById(R.id.textViewVhStat);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ue_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UEDTO ueDTO = getItem(position);
        holder.textViewCode.setText(ueDTO.ue.getCode());
        holder.textViewNom.setText(ueDTO.ue.getNom());
        holder.progressBar.setProgress(ueDTO.pourcentage());
        String percent = ueDTO.pourcentage() + "%";
        holder.tvChartPercentage.setText(percent);
        holder.textViewVolumehoraireStat.setText(ueDTO.volumeHoraireStat());

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(ueDTO);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            onItemClickListener.onItemLongClick(ueDTO);
            return false;
        });
    }

    public void setOnItemClickListener(OnItemClickListener<UEDTO> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
