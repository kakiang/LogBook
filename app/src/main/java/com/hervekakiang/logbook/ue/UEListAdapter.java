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

import java.util.ArrayList;
import java.util.List;

public class UEListAdapter extends ListAdapter<UEListAdapter.UeUiModel, UEListAdapter.ViewHolder> {

    public UEListAdapter() {
        super(DIFF_CALLBACK);
    }

    public record UeUiModel(
            UE ue,
            String volumeHoraireStat,
            int pourcentage) {}

    private static final DiffUtil.ItemCallback<UeUiModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<UeUiModel>() {
        @Override
        public boolean areItemsTheSame(@NonNull UeUiModel oldItem, @NonNull UeUiModel newItem) {
            return oldItem.ue.getId() == newItem.ue.getId() ;
        }

        @Override
        public boolean areContentsTheSame(@NonNull UeUiModel oldItem, @NonNull UeUiModel newItem) {
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
        UeUiModel ueUiModel = getItem(position);
        holder.textViewCode.setText(ueUiModel.ue.getCode());
        holder.textViewNom.setText(ueUiModel.ue.getNom());
        holder.progressBar.setProgress(ueUiModel.pourcentage());
        String percent = ueUiModel.pourcentage() + "%";
        holder.tvChartPercentage.setText(percent);
        holder.textViewVolumehoraireStat.setText(ueUiModel.volumeHoraireStat());
    }
}
