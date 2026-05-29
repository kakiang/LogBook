package com.hervekakiang.logbook;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;

public class BaseFragment extends Fragment {

    private NavController navController;
    private MaterialToolbar toolbar;

    public BaseFragment(@LayoutRes int contentLayoutId) {
        super(contentLayoutId);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        toolbar = view.findViewById(R.id.fragmentToolbar);
        if(toolbar != null) {
            AppBarConfiguration appBarConfiguration = NavigationConfig.getAppBarConfiguration();
            NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
        }
    }

    protected NavController getNavController() {
        if (navController == null){
            Log.e("BASEFRAGMENT MYAPP", "navController == null");
        }
        return navController;
    }

    protected MaterialToolbar getToolbar() {
        if (toolbar == null){
            Log.e("BASEFRAGMENT MYAPP", "navController == null ");
        }
        return toolbar;
    }
}
