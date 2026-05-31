package com.hervekakiang.logbook.dashboard;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.hervekakiang.logbook.BaseFragment;
import com.hervekakiang.logbook.BasicSwipeCallback;
import com.hervekakiang.logbook.MyAppViewModel;
import com.hervekakiang.logbook.OnItemClickListener;
import com.hervekakiang.logbook.R;
import com.hervekakiang.logbook.ue.UEListAdapter;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class DashboardFragment extends BaseFragment {

    private MyAppViewModel myAppViewModel;
    private UEListAdapter mAdapter;
    private RecyclerView recyclerView;

    public DashboardFragment() {
        super(R.layout.fragment_dashboard);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ProgressBar progressBar = view.findViewById(R.id.chartProgress);
        TextView tvChartPercentage = view.findViewById(R.id.tvChartPercentage);
        TextView tvVhEffectue = view.findViewById(R.id.tvVhEffectue);
        TextView tvVhRestant = view.findViewById(R.id.tvVhRestant);
        TextView tvVhTotal = view.findViewById(R.id.tvVhTotal);
        TextView textViewUeListTitle = view.findViewById(R.id.textViewUeListTitle);
        TextView tvNbUe = view.findViewById(R.id.tvNbUe);
        TextView tvNbMatiere = view.findViewById(R.id.tvNbMatiere);
        TextView tvNbSeance = view.findViewById(R.id.tvNbSeance);

        recyclerView = view.findViewById(R.id.recyclerviewUE);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ExtendedFloatingActionButton fab = view.findViewById(R.id.fab_add_seance);

        TextView layoutEmpty = view.findViewById(R.id.layoutEmpty);

        myAppViewModel = new ViewModelProvider(requireActivity()).get(MyAppViewModel.class);
        myAppViewModel.getStatsGlobal().observe(getViewLifecycleOwner(), stats -> {
            if (stats == null) return;
            Log.d("MYAPP:====stats=====", stats.toString());
            tvVhEffectue.setText(format(stats.effectue()));
            tvVhRestant.setText(format(Math.max(stats.total() - stats.effectue(), 0)));
            tvVhTotal.setText(format(stats.total()));
            int percentage = (stats.total() > 0) ? (stats.effectue() * 100) / stats.total() : 0;
            ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", 0, percentage);
            animator.setDuration(1000);
            animator.setInterpolator(new FastOutSlowInInterpolator());
            animator.start();
            var percentageText = format(percentage) + "%";
            tvChartPercentage.setText(percentageText);

            tvNbUe.setText(format(stats.nbUE()));
            tvNbMatiere.setText(format(stats.nbMatiere()));
            tvNbSeance.setText(format(stats.nbSeance()));
        });

        mAdapter = new UEListAdapter();
        mAdapter.setOnItemClickListener(listener());
        recyclerView.setAdapter(mAdapter);

        myAppViewModel.getListUEDTO().observe(getViewLifecycleOwner(), uedtos -> {
            String uet = "Unités d'enseignement (" + uedtos.size() + ")";
            textViewUeListTitle.setText(uet);
            if (uedtos.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                layoutEmpty.setVisibility(View.GONE);
                mAdapter.submitList(uedtos);
            }

        });

        fab.setOnClickListener(v -> getNavController().navigate(R.id.ajouterSeanceFragment));
        view.findViewById(R.id.btnAjouterUe).setOnClickListener(v -> getNavController().navigate(R.id.ajouterUeFragment));

        BasicSwipeCallback swipeCallback = getBasicSwipeCallback();
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);
    }

    private BasicSwipeCallback getBasicSwipeCallback() {
        BasicSwipeCallback.SwipeListener swipeListener = new BasicSwipeCallback.SwipeListener() {

            @Override
            public void onSwipeLeft(RecyclerView.ViewHolder viewHolder, int position) {
                var item = mAdapter.getCurrentList().get(position);

                myAppViewModel.deleteUeTemporarily(item.ue().getId());

                Snackbar.make(recyclerView, item.ue().getNom() + " supprimé", Snackbar.LENGTH_LONG)
                        .setAction("Annulé", v -> myAppViewModel.unDeleteUe())
                        .addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                super.onDismissed(transientBottomBar, event);
                                if (event != DISMISS_EVENT_ACTION) {
                                    myAppViewModel.deleteUe(item.ue().getId());
                                    Toast.makeText(
                                            recyclerView.getContext(),
                                            item.ue().getNom() + " supprimé avec succès",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        }).show();
            }

            @Override
            public void onSwipeRight(RecyclerView.ViewHolder viewHolder, int position) {
                mAdapter.notifyItemChanged(position);
                var item = mAdapter.getCurrentList().get(position);
                Bundle args = new Bundle();
                args.putInt("ueId", item.ue().getId());
                args.putBoolean("isEditing", true);
                Navigation.findNavController(requireActivity(), R.id.navHostFragment)
                        .navigate(R.id.action_dashboard_to_ajouterUeFragment, args);

            }
        };

        return new BasicSwipeCallback(swipeListener);
    }

    @NonNull
    private OnItemClickListener<UEListAdapter.UEDTO> listener() {
        return new OnItemClickListener<>() {
            @Override
            public void onItemClick(UEListAdapter.UEDTO ueDTO) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.navHostFragment);
                Bundle args = new Bundle();
                args.putSerializable("ueDTO", ueDTO);
                String fragmentTitle = ueDTO.ue().getCode() + " " + ueDTO.ue().getNom();
                args.putString("fragmentTitle", fragmentTitle);
                navController.navigate(R.id.ueDetailFragment, args);
            }

            @Override
            public void onItemLongClick(UEListAdapter.UEDTO uiModel) {

            }
        };
    }

    private String format(int val) {
        return String.format(Locale.getDefault(), "%d", val);
    }
}