package com.hervekakiang.logbook.ue;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.hervekakiang.logbook.matiere.Matiere;
import com.hervekakiang.logbook.matiere.MatiereDAO;
import com.hervekakiang.logbook.matiere.MatiereListAdapter;
import com.hervekakiang.logbook.seance.Seance;
import com.hervekakiang.logbook.seance.SeanceDAO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UEViewModel extends AndroidViewModel {
    private final UEDAO ueDao;
    private final SeanceDAO seanceDao;
    private final MatiereDAO matiereDao;

    private final MutableLiveData<Integer> currentUeId = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentMatiereId = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentSeanceId = new MutableLiveData<>();

    private final MutableLiveData<List<UE>> listUEs = new MutableLiveData<>();
    private final MutableLiveData<List<Matiere>> listMatieres = new MutableLiveData<>();
    private final MutableLiveData<List<Seance>> listSeances = new MutableLiveData<>();

    private final MediatorLiveData<List<UEListAdapter.UeWithStats>> listUEWithStats = new MediatorLiveData<>();

    private final LiveData<StatsGlobal> statsGlobal;
    private final LiveData<UEListAdapter.UeWithStats> currentUeWithStats;
    private final LiveData<Map<String, String>> currentSeanceObj;
    private final MediatorLiveData<MatiereListAdapter.MatiereWithStats> currentMatiereWithStats = new MediatorLiveData<>();

    private final LiveData<List<Seance>> seancesForCurrentMatiere;

    private final MediatorLiveData<List<MatiereListAdapter.MatiereWithStats>> matieresWithStatsForCurrentUe = new MediatorLiveData<>();
    private final MutableLiveData<Integer> pendingDeleteMatiereId = new MutableLiveData<>();
    private final MutableLiveData<Integer> pendingDeleteUeId = new MutableLiveData<>();

    public UEViewModel(@NonNull Application application) {
        super(application);
        ueDao = new UEDAO(application);
        seanceDao = new SeanceDAO(application);
        matiereDao = new MatiereDAO(application);

        refreshAllData();

        MediatorLiveData<CombinedData> combined = new MediatorLiveData<>();
        combined.addSource(listUEs, ues -> combineAndEmit(combined));
        combined.addSource(listMatieres, matieres -> combineAndEmit(combined));
        combined.addSource(listSeances, seances -> combineAndEmit(combined));

        listUEWithStats.addSource(combined, data -> computeUeWithStats(data, pendingDeleteUeId.getValue()));
        listUEWithStats.addSource(pendingDeleteUeId, hidden -> computeUeWithStats(combined.getValue(), hidden));

        statsGlobal = Transformations.map(listUEWithStats, ues -> {
            if (ues == null) return new StatsGlobal(0, 0);
            List<Matiere> allMatieres = listMatieres.getValue();
            List<Seance> allSeances = listSeances.getValue();
            if (allMatieres == null || allSeances == null) return new StatsGlobal(0, 0);
            int totalVh = 0;
            for (Matiere m : allMatieres) totalVh += m.getVolumeHoraire();
            int totalVhEffectue = 0;
            for (Seance s : allSeances) totalVhEffectue += s.getDuree();
            return new StatsGlobal(totalVh, totalVhEffectue);
        });

        currentMatiereWithStats.addSource(currentMatiereId, id -> updateCurrentMatiereStats());
        currentMatiereWithStats.addSource(listMatieres, matieres -> updateCurrentMatiereStats());
        currentMatiereWithStats.addSource(listSeances, seances -> updateCurrentMatiereStats());

        matieresWithStatsForCurrentUe.addSource(currentUeId, ueId ->
                computeMatieresWithStatsForSelectedUE(ueId, pendingDeleteMatiereId.getValue()));
        matieresWithStatsForCurrentUe.addSource(pendingDeleteMatiereId, hidden ->
                computeMatieresWithStatsForSelectedUE(currentUeId.getValue(), hidden));
        matieresWithStatsForCurrentUe.addSource(listMatieres, matieres ->
                computeMatieresWithStatsForSelectedUE(currentUeId.getValue(), pendingDeleteMatiereId.getValue()));

        currentUeWithStats = Transformations.switchMap(currentUeId, ueId -> {
            if (ueId == null || ueId == 0) {
                return new MutableLiveData<>(null);
            }

            return Transformations.map(listUEWithStats, uesWithStats -> {
                if (uesWithStats == null) return null;
                for (UEListAdapter.UeWithStats model : uesWithStats) {
                    if (model.ue().getId() == ueId) {
                        Log.d("MYAPP::UEViewModel", "currentUeWithStats:currentUeId changed to " + ueId);
                        return model;
                    }
                }
                return null;
            });
        });

        seancesForCurrentMatiere = Transformations.switchMap(currentMatiereId, matiereId -> {
            if (matiereId == null || matiereId == 0) {
                return new MutableLiveData<>(Collections.emptyList());
            }
            return Transformations.map(listSeances, allSeances -> {
                if (allSeances == null) return Collections.emptyList();
                List<Seance> filtered = new ArrayList<>();
                for (Seance s : allSeances) {
                    if (s.getMatiereId() == matiereId) {
                        filtered.add(s);
                    }
                }
                return filtered;
            });
        });

        currentSeanceObj = Transformations.switchMap(currentSeanceId, seanceDao::getSeanceById);

    }

    private void computeMatieresWithStatsForSelectedUE(Integer ueId, Integer hidden) {

        if (ueId == null || ueId == 0
                || listMatieres.getValue() == null
                || listMatieres.getValue().isEmpty()) {
            matieresWithStatsForCurrentUe.setValue(Collections.emptyList());
            return;
        }

        matieresWithStatsForCurrentUe.setValue(Collections.emptyList());

        List<Matiere> filteredMatieres = new ArrayList<>();
        for (Matiere m : listMatieres.getValue()) {
            if (m.getUeId() == ueId) {
                if (hidden != null && m.getId() == hidden) continue;
                filteredMatieres.add(m);
            }
        }

        List<Seance> allSeances = listSeances.getValue();
        if (allSeances == null) allSeances = Collections.emptyList();

        Map<Integer, Integer> volumeHoraireEffectueParMatiere = new HashMap<>();
        for (Seance s : allSeances) {
            int matId = s.getMatiereId();
            Integer current = volumeHoraireEffectueParMatiere.get(matId);
            if (current == null) current = 0;
            volumeHoraireEffectueParMatiere.put(matId, current + s.getDuree());
        }

        List<MatiereListAdapter.MatiereWithStats> result = new ArrayList<>();
        for (Matiere m : filteredMatieres) {
            int total = m.getVolumeHoraire();
            Integer vhEffectue = volumeHoraireEffectueParMatiere.get(m.getId());
            int effectue = (vhEffectue == null) ? 0 : vhEffectue;
            int percentage = total > 0 ? (effectue * 100) / total : 0;
            String statText = String.format(Locale.getDefault(), "%dH / %dH", effectue, total);
            result.add(new MatiereListAdapter.MatiereWithStats(m, statText, percentage));
        }
        matieresWithStatsForCurrentUe.postValue(result);
    }

    private record CombinedData(List<UE> ues, List<Matiere> matieres, List<Seance> seances) {
    }

    public record StatsGlobal(int total, int effectue) {
    }

    private void updateCurrentMatiereStats() {
        Integer matiereId = currentMatiereId.getValue();
        if (matiereId == null || matiereId == 0) {
            currentMatiereWithStats.setValue(null);
            return;
        }
        List<Matiere> allMatieres = listMatieres.getValue();
        List<Seance> allSeances = listSeances.getValue();
        if (allMatieres == null || allSeances == null) {
            return;
        }

        Matiere matiere = null;
        for (Matiere m : allMatieres) {
            if (m.getId() == matiereId) {
                matiere = m;
                break;
            }
        }
        if (matiere == null) {
            currentMatiereWithStats.setValue(null);
            return;
        }

        int effectue = 0;
        for (Seance s : allSeances) {
            if (s.getMatiereId() == matiereId) {
                effectue += s.getDuree();
            }
        }
        int total = matiere.getVolumeHoraire();
        int percentage = total > 0 ? (effectue * 100) / total : 0;
        String statText = String.format(Locale.getDefault(), "%dH / %dH", effectue, total);
        currentMatiereWithStats.setValue(new MatiereListAdapter.MatiereWithStats(matiere, statText, percentage));
    }

    private void combineAndEmit(MediatorLiveData<CombinedData> combined) {
        List<UE> ues = listUEs.getValue();
        List<Matiere> matieres = listMatieres.getValue();
        List<Seance> seances = listSeances.getValue();
        if (ues == null || matieres == null || seances == null) {
            return;
        }
        combined.setValue(new CombinedData(ues, matieres, seances));
    }

    private void computeUeWithStats(CombinedData combinedData, Integer hidden) {

        if (combinedData == null) {
            listUEWithStats.postValue(Collections.emptyList());
            return;
        }

        List<UE> ues = combinedData.ues();
        List<Matiere> matieres = combinedData.matieres();
        List<Seance> seances = combinedData.seances();

        if (ues == null || matieres == null || seances == null) {
            listUEWithStats.postValue(Collections.emptyList());
            return;
        }

        Map<Integer, Integer> volumeHoraireParUE = new HashMap<>();
        for (Matiere m : matieres) {
            int ueId = m.getUeId();
            Integer current = volumeHoraireParUE.get(ueId);
            if (current == null) current = 0;
            volumeHoraireParUE.put(ueId, current + m.getVolumeHoraire());
        }

        Map<Integer, Integer> matiereToUe = new HashMap<>();
        for (Matiere m : matieres) {
            matiereToUe.put(m.getId(), m.getUeId());
        }

        Map<Integer, Integer> volumeHoraireEffectureParUE = new HashMap<>();
        for (Seance s : seances) {
            Integer ueId = matiereToUe.get(s.getMatiereId());
            if (ueId != null) {
                Integer current = volumeHoraireEffectureParUE.get(ueId);
                if (current == null) current = 0;
                volumeHoraireEffectureParUE.put(ueId, current + s.getDuree());
            }
        }

        List<UEListAdapter.UeWithStats> result = new ArrayList<>();
        for (UE ue : ues) {
            if (hidden != null && ue.getId() == hidden) continue;
            int ueId = ue.getId();
            Integer volumeHoraire = volumeHoraireParUE.get(ueId);
            Integer horaireEffectue = volumeHoraireEffectureParUE.get(ueId);
            int total = (volumeHoraire == null) ? 0 : volumeHoraire;
            int effectue = (horaireEffectue == null) ? 0 : horaireEffectue;
            int percentage = (total > 0) ? (effectue * 100) / total : 0;
            String statText = String.format(Locale.getDefault(), "%dH dispensées / %dH", effectue, total);
            result.add(new UEListAdapter.UeWithStats(ue, statText, percentage));
        }
        listUEWithStats.postValue(result);
    }

    public void addUE(UE ue, Runnable onComplete) {
        ueDao.insert(ue, () -> {
            refreshUeData();
            if (onComplete != null) onComplete.run();
        });

    }

    public void updateUE(UE ue, Runnable onComplete) {
        Log.d("updateUE MYAPP", ue.toString());
        ueDao.update(ue, () -> {
            refreshUeData();
            if (onComplete != null) onComplete.run();
        });
    }

    public void addMatiere(Matiere matiere, int ueId, Runnable onComplete) {
        matiereDao.insert(matiere, () -> {
            refreshAllData();
            currentUeId.postValue(ueId);
            if (onComplete != null) onComplete.run();
        });
    }

    public void updateMatiere(Matiere matiere, Runnable onComplete) {
        Log.d("updateMatiere MYAPP", matiere.toString());
        matiereDao.update(matiere, () -> {
            refreshAllData();
            if (onComplete != null) onComplete.run();
        });
    }

    public void deleteMatiere(int matiereId) {
        if (pendingDeleteMatiereId.getValue() != null && pendingDeleteMatiereId.getValue() == matiereId) {
            pendingDeleteMatiereId.postValue(null);
        }
        matiereDao.delete(matiereId, this::refreshAllData);
    }

    public void deleteMatiereTemporarily(int matiereId) {
        pendingDeleteMatiereId.postValue(matiereId);
    }

    public void unDeleteMatiere() {
        pendingDeleteMatiereId.postValue(null);
    }

    public void deleteUe(int ueId) {
        if (pendingDeleteUeId.getValue() != null && pendingDeleteUeId.getValue() == ueId) {
            pendingDeleteUeId.postValue(null);
        }
        ueDao.delete(ueId, this::refreshAllData);
    }

    public void deleteUeTemporarily(int ueId) {
        pendingDeleteUeId.postValue(ueId);
    }

    public void unDeleteUe() {
        pendingDeleteUeId.postValue(null);
    }

    public void deleteSeance(int seanceId) {
        seanceDao.delete(seanceId, this::refreshAllData);
    }

    public void addSeance(Seance seance, Runnable onComplete) {
        seanceDao.insert(seance, () -> {
            refreshAllData();
            Integer currentId = currentMatiereId.getValue();
            if (currentId != null) {
                currentMatiereId.postValue(currentId);
            }
            if (onComplete != null) onComplete.run();
        });
    }

    public void updateSeance(Seance seance, Runnable onComplete) {
        Log.d("updateSeance MYAPP", seance.toString());
        seanceDao.update(seance, () -> {
            refreshAllData();
            if (onComplete != null) onComplete.run();
        });
    }

    public void refreshAllData() {
        ueDao.getAll(listUEs::postValue);
        matiereDao.getAll(listMatieres::postValue);
        seanceDao.getAll(listSeances::postValue);
    }

    public void refreshUeData() {
        ueDao.getAll(listUEs::postValue);
    }

    public void setCurrentUeId(int currentUeId) {
        this.currentUeId.setValue(currentUeId);
    }

    public void setCurrentMatiereId(int currentMatiereId) {
        this.currentMatiereId.setValue(currentMatiereId);
    }

    public void setCurrentSeanceId(int currentSeanceId) {
        this.currentSeanceId.setValue(currentSeanceId);
    }

    public LiveData<List<Matiere>> getListMatieres() {
        return listMatieres;
    }

    public LiveData<List<UE>> getListUEs() {
        return listUEs;
    }

    public LiveData<StatsGlobal> getStatsGlobal() {
        return statsGlobal;
    }

    public LiveData<List<UEListAdapter.UeWithStats>> getListUEWithStats() {
        return listUEWithStats;
    }

    public LiveData<UEListAdapter.UeWithStats> getCurrentUeWithStats() {
        return currentUeWithStats;
    }

    public LiveData<List<MatiereListAdapter.MatiereWithStats>> getMatieresWithStatsForCurrentUe() {
        return matieresWithStatsForCurrentUe;
    }

    public LiveData<List<Seance>> getSeancesForCurrentMatiere() {
        return seancesForCurrentMatiere;
    }

    public LiveData<MatiereListAdapter.MatiereWithStats> getCurrentMatiereWithStats() {
        return currentMatiereWithStats;
    }

    public LiveData<Map<String, String>> getCurrentSeanceObj() {
        return currentSeanceObj;
    }
}