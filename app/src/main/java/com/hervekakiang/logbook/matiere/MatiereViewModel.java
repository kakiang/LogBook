package com.hervekakiang.logbook.matiere;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class MatiereViewModel extends AndroidViewModel {
    private final MatiereDAO matiereDao;
    private final MutableLiveData<List<Matiere>> listMatieres = new MutableLiveData<>();

    public MatiereViewModel(Application application) {
        super(application);
        matiereDao = new MatiereDAO(application);
        refreshList();
    }

    public void addMatiere(Matiere matiere) {
        matiereDao.insert(matiere, this::refreshList);
    }

    public LiveData<List<Matiere>> getListMatieres() {
        return listMatieres;
    }

    public int getTotalVolumeHoraireByUeId(int ueId) {
        return matiereDao.getTotalVolumeHoraireByUeId(ueId);
    }

    private void refreshList(){
        matiereDao.getAll(listMatieres::postValue);
    }
}