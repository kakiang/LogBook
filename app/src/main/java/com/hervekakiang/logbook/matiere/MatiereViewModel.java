package com.hervekakiang.logbook.matiere;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.hervekakiang.logbook.seance.Seance;
import com.hervekakiang.logbook.seance.SeanceDAO;

import java.util.List;

public class MatiereViewModel extends AndroidViewModel {

    private final MatiereDAO matiereDao;
    private final SeanceDAO seanceDao;

    public MatiereViewModel(Application application) {
        super(application);
        matiereDao = new MatiereDAO(application);
        seanceDao = new SeanceDAO(application);
    }

    public LiveData<Matiere> getMatiereById(int matiereId) {
        return matiereDao.getMatiereById(matiereId);
    }

    public int getTotalVhDispenseByMatiere(int matiereId) {
        return seanceDao.getTotalVhDispenseByMatiere(matiereId);
    }
}
