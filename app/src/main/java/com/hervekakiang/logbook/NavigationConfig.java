package com.hervekakiang.logbook;

import androidx.navigation.ui.AppBarConfiguration;

import java.util.HashSet;
import java.util.Set;

public final class NavigationConfig {

    private static final Set<Integer> TOP_LEVEL_IDS = new HashSet<>();

    static {
        TOP_LEVEL_IDS.add(R.id.dashboardFragment);
        TOP_LEVEL_IDS.add(R.id.matiereListFragment);
        TOP_LEVEL_IDS.add(R.id.seanceListFragment);
    }

    private NavigationConfig() {
    }

    public static boolean isTopLevel(int destinationId) {
        return TOP_LEVEL_IDS.contains(destinationId);
    }

    public static AppBarConfiguration getAppBarConfiguration() {
        return new AppBarConfiguration.Builder(TOP_LEVEL_IDS).build();
    }
}
