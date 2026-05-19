package com.hervekakiang.logbook;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hervekakiang.logbook.matiere.MatiereViewModel;
import com.hervekakiang.logbook.seance.SeanceViewModel;

public class ViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final int id;

    public ViewModelFactory(Application application, int id) {
        this.application = application;
        this.id = id;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MatiereViewModel.class)) {
            return (T) new MatiereViewModel(application, id);
        }
        if (modelClass.isAssignableFrom(SeanceViewModel.class)) {
            return (T) new SeanceViewModel(application, id);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
