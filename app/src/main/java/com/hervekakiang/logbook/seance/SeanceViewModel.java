package com.hervekakiang.logbook.seance;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class SeanceViewModel extends AndroidViewModel {
    private Integer matiereId;
    private final SeanceDAO seanceDao;
    private final MutableLiveData<List<Seance>> listSeances = new MutableLiveData<>();

    public SeanceViewModel(Application application) {
        super(application);
        seanceDao = new SeanceDAO(application);
        refreshList();
    }

    public SeanceViewModel(Application application, int matiereId) {
        super(application);
        this.matiereId = matiereId;
        seanceDao = new SeanceDAO(application);
        refreshList();
    }

    public void addSeance(Seance seance) {
        seanceDao.insert(seance, this::refreshList);
    }

    public LiveData<List<Seance>> getListSeances() {
        return listSeances;
    }

    public int getTotalVolumeHoraireEffectue() {
        return seanceDao.getTotalVolumeHoraireEffectue();
    }

    public int getTotalVolumeHoraireEffectueByUeId(int ueId) {
        return seanceDao.getTotalVolumeHoraireEffectueByUeId(ueId);
    }

    private void refreshList() {
        if (matiereId != null) {
            Log.d("MATIEREID", String.valueOf(matiereId));
            seanceDao.getSeancesByMatiereId(matiereId, listSeances::postValue);
        } else {
            seanceDao.getAll(listSeances::postValue);
        }
    }
}