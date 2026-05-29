package com.hervekakiang.logbook.ue;

import android.animation.ObjectAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.hervekakiang.logbook.MyAppViewModel;
import com.hervekakiang.logbook.NavigationConfig;
import com.hervekakiang.logbook.OnItemClickListener;
import com.hervekakiang.logbook.R;
import com.hervekakiang.logbook.matiere.MatiereListAdapter;

import java.util.Locale;

public class UeDetailFragment extends Fragment {
    private UEListAdapter.UEDTO ueDTO;
    private MatiereListAdapter mAdapter;
    private MyAppViewModel myAppViewModel;
    private RecyclerView recyclerView;

    public UeDetailFragment() {
    }

    public static UeDetailFragment newInstance(UE ue) {
        UeDetailFragment fragment = new UeDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("ue", ue);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ue_detail, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController navController = Navigation.findNavController(view);

        if (getArguments() != null) {
            ueDTO = (UEListAdapter.UEDTO) getArguments().getSerializable("ueWithStats");
        } else {
            navController.navigateUp();
        }

        AppBarConfiguration appBarConfiguration = NavigationConfig.getAppBarConfiguration();
        MaterialToolbar fragmentToolbar = view.findViewById(R.id.fragmentToolbar);
        NavigationUI.setupWithNavController(fragmentToolbar, navController, appBarConfiguration);

        ProgressBar progressBar = view.findViewById(R.id.chartProgress);
        TextView tvChartPercentage = view.findViewById(R.id.tvChartPercentage);
        TextView textViewVhStat = view.findViewById(R.id.textViewVhStat);
        TextView tvMatiereListTitle = view.findViewById(R.id.tvMatiereListTitle);
        fragmentToolbar.setTitle(ueDTO.ue().getCode() + " " + ueDTO.ue().getNom());

        myAppViewModel = new ViewModelProvider(requireActivity()).get(MyAppViewModel.class);
        myAppViewModel.setCurrentUeId(ueDTO.ue().getId());

        myAppViewModel.getCurrentUEDTO().observe(getViewLifecycleOwner(), ueWithStats -> {
            ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", 0, ueWithStats.pourcentage());
            animator.setDuration(1000);
            animator.setInterpolator(new FastOutSlowInInterpolator());
            animator.start();

            tvChartPercentage.setText(String.format(Locale.getDefault(), "%d%%", ueWithStats.pourcentage()));
            textViewVhStat.setText(ueWithStats.volumeHoraireStat());
            fragmentToolbar.setTitle(ueWithStats.ue().getCode() + " " + ueWithStats.ue().getNom());
        });

        ExtendedFloatingActionButton fab = view.findViewById(R.id.fabAddMatiere);
        fab.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("selectedUeId", ueDTO.ue().getId());
            Navigation.findNavController(requireActivity(), R.id.navHostFragment).navigate(R.id.action_to_ajouterMatiereFragment, args);
        });

        recyclerView = view.findViewById(R.id.recyclerviewMatiere);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new MatiereListAdapter();

        mAdapter.setOnItemClickListener(listener());
        recyclerView.setAdapter(mAdapter);
        ItemTouchHelper itemTouchHelper = getItemTouchHelper();
        itemTouchHelper.attachToRecyclerView(recyclerView);

        myAppViewModel.getListMatiereDTOForCurrentUE().observe(getViewLifecycleOwner(), matiereWithStatsList -> {
            String mt = "Matières (" + matiereWithStatsList.size() + ")";
            tvMatiereListTitle.setText(mt);
            mAdapter.submitList(matiereWithStatsList);
        });

    }

    private OnItemClickListener<MatiereListAdapter.MatiereDTO> listener() {
        return new OnItemClickListener<>() {
            @Override
            public void onItemClick(MatiereListAdapter.MatiereDTO matiereDTO) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.navHostFragment);
                Bundle args = new Bundle();
                Log.d("MYAPP::UeDetailFragment", "matiereId=" + matiereDTO.matiere().getId());
                args.putInt("matiereId", matiereDTO.matiere().getId());
                args.putString("fragmentTitle", matiereDTO.matiere().getNom());
                navController.navigate(R.id.matiereDetailFragment, args);
            }

            @Override
            public void onItemLongClick(MatiereListAdapter.MatiereDTO obj) {

            }
        };
    }

    private ItemTouchHelper getItemTouchHelper() {

        return new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAbsoluteAdapterPosition();
                var item = mAdapter.getCurrentList().get(position);

                if (direction == ItemTouchHelper.LEFT) {
                    myAppViewModel.deleteMatiereTemporarily(item.matiere().getId());
                    Snackbar.make(recyclerView, "Matiere supprimée", Snackbar.LENGTH_LONG)
                            .setAction("Annulé", v -> {
                                myAppViewModel.unDeleteMatiere();
                            }).addCallback(new Snackbar.Callback() {
                                @Override
                                public void onDismissed(Snackbar transientBottomBar, int event) {
                                    super.onDismissed(transientBottomBar, event);
                                    if (event != DISMISS_EVENT_ACTION) {
                                        myAppViewModel.deleteMatiere(item.matiere().getId());
                                        Toast.makeText(
                                                recyclerView.getContext(),
                                                item.matiere().getNom() + " supprimé avec succès",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            })
                            .show();
                } else if (direction == ItemTouchHelper.RIGHT) {
                    mAdapter.notifyItemChanged(position);
                    NavController nav = Navigation.findNavController(requireActivity(), R.id.navHostFragment);
                    Bundle args = new Bundle();
                    args.putInt("selectedUeId", ueDTO.ue().getId());
                    args.putInt("matiereId", item.matiere().getId());
                    args.putBoolean("isEditing", true);
                    nav.navigate(R.id.action_to_ajouterMatiereFragment, args);
                }

            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    Paint bgPaint = new Paint();
                    RectF background;
                    Drawable icon;
                    int iconMargin, iconTop, iconBottom, iconLeft, iconRight;

                    if (dX > 0) {// right, edit
                        bgPaint.setColor(Color.parseColor("#2E7D32"));
                        background = new RectF(
                                itemView.getLeft(),
                                itemView.getTop(),
                                itemView.getLeft() + dX,
                                itemView.getBottom()
                        );
                        c.drawRoundRect(background, 16f, 16f, bgPaint);

                        icon = ContextCompat.getDrawable(recyclerView.getContext(), R.drawable.ic_edit_24);

                        if (icon == null) return;
                        iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                        iconTop = itemView.getTop() + iconMargin;
                        iconBottom = itemView.getBottom() - iconMargin;
                        iconLeft = itemView.getLeft() + iconMargin;
                        iconRight = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();

                        if (dX > iconMargin) {
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                            icon.setTint(Color.WHITE);
                            icon.draw(c);
                        }

                    } else if (dX < 0) {
                        bgPaint.setColor(Color.parseColor("#B00020"));
                        background = new RectF(
                                itemView.getRight() + dX,
                                itemView.getTop(),
                                itemView.getRight(),
                                itemView.getBottom()
                        );
                        c.drawRoundRect(background, 16f, 16f, bgPaint);
                        icon = ContextCompat.getDrawable(recyclerView.getContext(), R.drawable.ic_delete_outline);

                        if (icon == null) return;
                        iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                        iconTop = itemView.getTop() + iconMargin;
                        iconBottom = itemView.getBottom() - iconMargin;
                        iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
                        iconRight = itemView.getRight() - iconMargin;

                        if (itemView.getRight() + dX < iconLeft) {
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                            icon.setTint(Color.WHITE);
                            icon.draw(c);
                        }
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            }
        });
    }
}