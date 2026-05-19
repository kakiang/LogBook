package com.hervekakiang.logbook.matiere;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hervekakiang.logbook.seance.Seance;
import com.hervekakiang.logbook.seance.SeanceDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MatiereViewModel extends AndroidViewModel {
    private int ueId;
    private final MatiereDAO matiereDao;
    private final SeanceDAO seanceDao;
    private final MutableLiveData<List<Matiere>> listMatieres = new MutableLiveData<>();
    private final MutableLiveData<List<Seance>> listSeances = new MutableLiveData<>();
    private final MediatorLiveData<List<MatiereListAdapter.MatiereWithStats>> matieresWithStats = new MediatorLiveData<>();

    public MatiereViewModel(Application application) {
        super(application);
        matiereDao = new MatiereDAO(application);
        seanceDao = new SeanceDAO(application);
        refreshList();

        matieresWithStats.addSource(listMatieres, matieres -> combineAndProcess());
        matieresWithStats.addSource(listSeances, seances -> combineAndProcess());
    }

    public MatiereViewModel(Application application, int ueId) {
        super(application);
        this.ueId = ueId;
        matiereDao = new MatiereDAO(application);
        seanceDao = new SeanceDAO(application);
        refreshList();

        matieresWithStats.addSource(listMatieres, matieres -> combineAndProcess());
        matieresWithStats.addSource(listSeances, seances -> combineAndProcess());
    }

    public void addMatiere(Matiere matiere) {
        matiereDao.insert(matiere, this::refreshList);
    }

    public void setUeId(int ueId) {
        this.ueId = ueId;
        refreshList();
    }

    public int getUeId() {
        return ueId;
    }

    public LiveData<List<Matiere>> getListMatieres() {
        return listMatieres;
    }

    public LiveData<List<MatiereListAdapter.MatiereWithStats>> getMatieresWithStats() {
        return matieresWithStats;
    }

    private void refreshList() {
        if (ueId != 0) {
            Log.d("UEID", String.valueOf(ueId));
            matiereDao.getMatieresByUeId(ueId, listMatieres::postValue);
        } else {
            matiereDao.getAll(listMatieres::postValue);
        }
        seanceDao.getAll(listSeances::postValue);
    }

    private void combineAndProcess() {
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

//    public static class Factory implements ViewModelProvider.Factory {
//        private final Application application;
//        private final int ueId;
//
//        public Factory(Application application, int ueId) {
//            this.application = application;
//            this.ueId = ueId;
//        }
//
//        @NonNull
//        @Override
//        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
//            if (modelClass.isAssignableFrom(MatiereViewModel.class)) {
//                return (T) new MatiereViewModel(application, ueId);
//            }
//            throw new IllegalArgumentException("Unknown ViewModel class");
//        }
//    }
}