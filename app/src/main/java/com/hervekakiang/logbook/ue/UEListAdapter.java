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

import com.hervekakiang.logbook.R;

import java.io.Serializable;

public class UEListAdapter extends ListAdapter<UEListAdapter.UeWithStats, UEListAdapter.ViewHolder> {
    private OnItemClickListener onItemClickListener;


    public interface OnItemClickListener {
        void onItemClick(UeWithStats ue);
        void onItemLongClick(UeWithStats ue);
    }
    public UEListAdapter() {
        super(DIFF_CALLBACK);
    }

    public UEListAdapter(OnItemClickListener onItemClickListener) {
        super(DIFF_CALLBACK);
        this.onItemClickListener = onItemClickListener;
    }

    public record UeWithStats(
            UE ue,
            String volumeHoraireStat,
            int pourcentage) implements Serializable {}

    private static final DiffUtil.ItemCallback<UeWithStats> DIFF_CALLBACK = new DiffUtil.ItemCallback<UeWithStats>() {
        @Override
        public boolean areItemsTheSame(@NonNull UeWithStats oldItem, @NonNull UeWithStats newItem) {
            return oldItem.ue.getId() == newItem.ue.getId() ;
        }

        @Override
        public boolean areContentsTheSame(@NonNull UeWithStats oldItem, @NonNull UeWithStats newItem) {
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
        UeWithStats ueWithStats = getItem(position);
        holder.textViewCode.setText(ueWithStats.ue.getCode());
        holder.textViewNom.setText(ueWithStats.ue.getNom());
        holder.progressBar.setProgress(ueWithStats.pourcentage());
        String percent = ueWithStats.pourcentage() + "%";
        holder.tvChartPercentage.setText(percent);
        holder.textViewVolumehoraireStat.setText(ueWithStats.volumeHoraireStat());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(ueWithStats);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onItemClickListener.onItemLongClick(ueWithStats);
                return false;
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
