package com.hervekakiang.logbook.ue;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.hervekakiang.logbook.R;

import java.util.ArrayList;
import java.util.List;

public class UEListAdapter extends ListAdapter<UE, UEListAdapter.ViewHolder> {//<UEListAdapter.ViewHolder> {

    public UEListAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<UE> DIFF_CALLBACK = new DiffUtil.ItemCallback<UE>() {
        @Override
        public boolean areItemsTheSame(@NonNull UE oldItem, @NonNull UE newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull UE oldItem, @NonNull UE newItem) {
            return oldItem.getNom().equals(newItem.getNom()) &&
                    oldItem.getCode().equals(newItem.getCode());
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewCode;
        private final TextView textViewNom;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewCode = itemView.findViewById(R.id.textViewCode);
            textViewNom = itemView.findViewById(R.id.textViewNom);        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ue_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UE ue = getItem(position);
        holder.textViewCode.setText(ue.getCode());
        holder.textViewNom.setText(ue.getNom());
    }
}
