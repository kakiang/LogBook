package com.hervekakiang.logbook.matiere;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.hervekakiang.logbook.seance.Seance;
import com.hervekakiang.logbook.seance.SeanceDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MatiereViewModel extends AndroidViewModel {

    private final MatiereDAO matiereDao;
    private final SeanceDAO seanceDao;

    private final MutableLiveData<Integer> currentMatiereId = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentUeId = new MutableLiveData<>();

    private final MutableLiveData<List<Matiere>> listMatieres = new MutableLiveData<>();
    private final MutableLiveData<List<Seance>> listSeances = new MutableLiveData<>();

    private final LiveData<MatiereListAdapter.MatiereWithStats> currentMatiereWithStats;
    private final MediatorLiveData<List<MatiereListAdapter.MatiereWithStats>> matieresWithStats = new MediatorLiveData<>();

    public MatiereViewModel(Application application) {
        super(application);
        matiereDao = new MatiereDAO(application);
        seanceDao = new SeanceDAO(application);

        loadSeances();
        currentUeId.observeForever(this::loadMatieres);
        setCurrentUeId(0);

        matieresWithStats.addSource(listMatieres, matieres -> calculHoraireEffectueAndPourcentage());
        matieresWithStats.addSource(listSeances, seances -> calculHoraireEffectueAndPourcentage());

        currentMatiereWithStats = Transformations.switchMap(currentMatiereId, matiereId -> {
            MutableLiveData<MatiereListAdapter.MatiereWithStats> result = new MutableLiveData<>();
            if (matiereId == null || matiereId == 0) {
                result.setValue(null);
                return result;
            }
            matiereDao.getExecutorService().execute(() -> {
                Matiere matiere = matiereDao.getMatiereById(matiereId);
                if (matiere == null) return;
                int horaireEffectue = seanceDao.getTotalVolumeHoraireEffectueByMatiereId(matiere.getId());
                int percentage = (matiere.getVolumeHoraire() > 0)
                        ? (horaireEffectue * 100) / matiere.getVolumeHoraire() : 0;
                String volumeHoraireStat = String.format(Locale.getDefault(),
                        "%dH / %dH", horaireEffectue, matiere.getVolumeHoraire());
                MatiereListAdapter.MatiereWithStats mws =
                        new MatiereListAdapter.MatiereWithStats(matiere, volumeHoraireStat, percentage);
                result.postValue(mws);
            });
            return result;
        });


    }

    public void addMatiere(Matiere matiere) {
        matiereDao.insert(matiere, () -> {
            loadMatieres(currentUeId.getValue());
        });
    }

    public LiveData<List<MatiereListAdapter.MatiereWithStats>> getMatieresWithStats() {
        return matieresWithStats;
    }

    private void loadSeances() {
        seanceDao.getAll(listSeances::postValue);
    }

    // Call this after any change to seances (e.g., add/delete/edit seance)
    public void refreshSeances() {
        loadSeances();
    }

    private void loadMatieres(Integer ueId) {
        if (ueId != null && ueId != 0) {
            matiereDao.getMatieresByUeId(ueId, listMatieres::postValue);
        } else {
            matiereDao.getAll(listMatieres::postValue);
        }
    }

    public LiveData<MatiereListAdapter.MatiereWithStats> getCurrentMatiereWithStats() {
        return currentMatiereWithStats;
    }

    private void calculHoraireEffectueAndPourcentage() {
        List<Matiere> matieres = listMatieres.getValue();
        if (matieres == null) return;
        matiereDao.getExecutorService().execute(() -> {
            List<MatiereListAdapter.MatiereWithStats> matieresWithStats = new ArrayList<>();
            for (Matiere matiere : matieres) {
                int horaireEffectue = seanceDao.getTotalVolumeHoraireEffectueByMatiereId(matiere.getId());
                int percentage = (matiere.getVolumeHoraire() > 0) ? (horaireEffectue * 100) / matiere.getVolumeHoraire() : 0;
                String volumeHoraireStat = String.format(Locale.getDefault(), "%dH / %dH", horaireEffectue, matiere.getVolumeHoraire());
                matieresWithStats.add(new MatiereListAdapter.MatiereWithStats(matiere, volumeHoraireStat, percentage));
            }
            this.matieresWithStats.postValue(matieresWithStats);
        });

    }

    public void setCurrentMatiereId(int currentMatiereId) {
        this.currentMatiereId.setValue(currentMatiereId);
    }

    public void setCurrentUeId(int currentUeId) {
        this.currentUeId.setValue(currentUeId);
    }

    public LiveData<List<Matiere>> getListMatieres() {
        return listMatieres;
    }
}