package com.hervekakiang.logbook.seance;

import android.app.Application;
import android.service.autofill.Transformation;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

public class SeanceViewModel extends AndroidViewModel {
    private final MutableLiveData<Integer> matiereId = new MutableLiveData<>();
    private final SeanceDAO seanceDao;
    private final LiveData<List<Seance>> listSeances;

    public SeanceViewModel(Application application) {
        super(application);
        seanceDao = new SeanceDAO(application);

        this.listSeances = Transformations.switchMap(matiereId, id ->{
            if (id == null || id == 0) {
                return seanceDao.getAll();
            }
            return seanceDao.getSeancesByMatiereId(id);
        });
        setMatiereId(0);
    }

    public void addSeance(Seance seance) {
        seanceDao.insert(seance, () -> {
            Integer currentId = matiereId.getValue();
            if (currentId != null && currentId != 0) {
                matiereId.postValue(currentId);
            } else {
                matiereId.postValue(0);
            }
        });
    }

    public LiveData<List<Seance>> getListSeances() {
        return listSeances;
    }

    public void setMatiereId(int matiereId) {
        this.matiereId.setValue(matiereId);
    }
}