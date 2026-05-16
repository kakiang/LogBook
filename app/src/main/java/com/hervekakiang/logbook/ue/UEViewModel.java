package com.hervekakiang.logbook.ue;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.hervekakiang.logbook.matiere.Matiere;
import com.hervekakiang.logbook.matiere.MatiereDAO;
import com.hervekakiang.logbook.seance.Seance;
import com.hervekakiang.logbook.seance.SeanceDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UEViewModel extends AndroidViewModel {
    private final UEDAO ueDao;
    private final SeanceDAO seanceDao;
    private final MatiereDAO matiereDao;

    private final MediatorLiveData<List<UEListAdapter.UeUiModel>> ueUiModels = new MediatorLiveData<>();
    private final MutableLiveData<List<UE>> listUEs = new MutableLiveData<>();
    private final MutableLiveData<List<Matiere>> listMatieres = new MutableLiveData<>();
    private final MutableLiveData<List<Seance>> listSeances = new MutableLiveData<>();

    public UEViewModel(@NonNull Application application) {
        super(application);
        ueDao = new UEDAO(application);
        seanceDao = new SeanceDAO(application);
        matiereDao = new MatiereDAO(application);
        refreshAllList();

        ueUiModels.addSource(listUEs, ues -> combineAndProcess());
        ueUiModels.addSource(listMatieres, matieres -> combineAndProcess());
        ueUiModels.addSource(listSeances, seances -> combineAndProcess());
    }

    public void addUE(UE ue) {
        ueDao.insert(ue, this::refreshAllList);
    }

    public void refreshAllList(){
        ueDao.getAll(listUEs::postValue);
        matiereDao.getAll(listMatieres::postValue);
        seanceDao.getAll(listSeances::postValue);
        Log.d("UEViewModel", "refreshList is called");
    }

    public LiveData<List<UE>> getListUEs() {
        return listUEs;
    }

    public LiveData<List<UEListAdapter.UeUiModel>> getUeUiModels() {
        return ueUiModels;
    }

    private void combineAndProcess() {
        List<UE> ues = listUEs.getValue();
        if (ues == null) return;

        ueDao.getExecutorService().execute(()->{
            List<UEListAdapter.UeUiModel> map = new ArrayList<>();
            for (UE ue : ues) {
                int horaireEffectue = seanceDao.getTotalVolumeHoraireEffectueByUeId(ue.getId());
                int horaireTotal = matiereDao.getTotalVolumeHoraireByUeId(ue.getId());

                int percentage = (horaireTotal > 0) ? (horaireEffectue * 100) / horaireTotal : 0;
                String volumeHoraireStat = String.format(Locale.getDefault(),"%dh / %dh", horaireEffectue, horaireTotal);

                map.add(new UEListAdapter.UeUiModel(ue, volumeHoraireStat, percentage));
            }
            ueUiModels.postValue(map);
        });
    }
}