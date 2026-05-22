package com.hervekakiang.logbook;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentContainerView;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hervekakiang.logbook.databinding.ActivityMainBinding;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final Set<Integer> TOP_LEVEL_DESTINATIONS = new HashSet<Integer>();

    static {
        TOP_LEVEL_DESTINATIONS.add(R.id.dashboardFragment);
//        TOP_LEVEL_DESTINATIONS.add(R.id.ueListFragment);
        TOP_LEVEL_DESTINATIONS.add(R.id.matiereListFragment);
        TOP_LEVEL_DESTINATIONS.add(R.id.seanceListFragment);
    }

    private NavController navController;
//    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        topAppBar = binding.topAppBar;
        bottomNavigationView = binding.bottomNavigationView;

        setupNavigation();
//        setupToolbarActions();
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
        FragmentContainerView fragmentContainerView = findViewById(R.id.navHostFragment);
        if (navHostFragment == null) {
            throw new IllegalStateException("NavHostFragment R.id.navHostFragment not found");
        }
        navController = navHostFragment.getNavController();

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController navController, @NonNull NavDestination navDestination, @Nullable Bundle bundle) {
                boolean isTopLevel = TOP_LEVEL_DESTINATIONS.contains(navDestination.getId());

                if (isTopLevel) {
//                    fragmentContainerView.setPadding(16,0,16,88);
                    bottomNavigationView.animate()
                            .translationY(0)
                            .setDuration(200)
                            .withStartAction(new Runnable() {
                                @Override
                                public void run() {
                                    bottomNavigationView.setVisibility(View.VISIBLE);
                                }
                            }).start();
                } else {
                    fragmentContainerView.setPadding(16,0,16,0);
                    bottomNavigationView.animate()
                            .translationY(bottomNavigationView.getHeight())
                            .setDuration(200)
                            .withEndAction(() -> bottomNavigationView.setVisibility(View.GONE))
                            .start();
                }

//                if (bundle != null && bundle.containsKey("fragmentTitle")) {
//                    String fragmentTitle = bundle.getString("fragmentTitle");
//                    topAppBar.setTitle(fragmentTitle);
//                }

            }
        });
//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(TOP_LEVEL_DESTINATIONS).build();
//        NavigationUI.setupWithNavController(topAppBar, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }

//    private void setupToolbarActions() {
//        topAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                int itemId = item.getItemId();
//
//                if (itemId == R.id.action_search) {
////                    navController.navigate(R.id.searchFragment);
//                    Toast.makeText(MainActivity.this, "Search", Toast.LENGTH_SHORT).show();
//                    return true;
//                }
//                return NavigationUI.onNavDestinationSelected(item, navController);
//            }
//        });
//    }
}