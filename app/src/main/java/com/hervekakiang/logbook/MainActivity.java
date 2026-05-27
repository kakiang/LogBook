package com.hervekakiang.logbook;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hervekakiang.logbook.databinding.ActivityMainBinding;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final Set<Integer> TOP_LEVEL_DESTINATIONS = new HashSet<>();

    static {
        TOP_LEVEL_DESTINATIONS.add(R.id.dashboardFragment);
//        TOP_LEVEL_DESTINATIONS.add(R.id.matiereListFragment);
        TOP_LEVEL_DESTINATIONS.add(R.id.seanceListFragment);
    }

    private BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bottomNavigationView = binding.bottomNavigationView;

        setupNavigation();
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
        FragmentContainerView fragmentContainerView = findViewById(R.id.navHostFragment);
        if (navHostFragment == null) {
            throw new IllegalStateException("NavHostFragment R.id.navHostFragment not found");
        }
        NavController navController = navHostFragment.getNavController();

        navController.addOnDestinationChangedListener((navController1, navDestination, bundle) -> {
            boolean isTopLevel = TOP_LEVEL_DESTINATIONS.contains(navDestination.getId());

            if (isTopLevel) {
                bottomNavigationView.animate()
                        .translationY(0)
                        .setDuration(200)
                        .withStartAction(() -> bottomNavigationView.setVisibility(View.VISIBLE))
                        .start();
            } else {
                fragmentContainerView.setPadding(16,0,16,0);
                bottomNavigationView.animate()
                        .translationY(bottomNavigationView.getHeight())
                        .setDuration(200)
                        .withEndAction(() -> bottomNavigationView.setVisibility(View.GONE))
                        .start();
            }
        });
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }
}