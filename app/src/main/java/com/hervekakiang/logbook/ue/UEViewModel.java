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

    private final MutableLiveData<List<UE>> listUEs = new MutableLiveData<>();
    private final MutableLiveData<List<Matiere>> listMatieres = new MutableLiveData<>();
    private final MutableLiveData<List<Seance>> listSeances = new MutableLiveData<>();

    // Derived UI models (calculated from caches)
    private final LiveData<List<UEListAdapter.UeWithStats>> ueWithStatsList;

    private final LiveData<StatsGlobal> statsGlobal;
    private final LiveData<UEListAdapter.UeWithStats> currentUeWithStats;
//    private final LiveData<MatiereListAdapter.MatiereWithStats> currentMatiereWithStats;
    private final MediatorLiveData<MatiereListAdapter.MatiereWithStats> currentMatiereWithStats = new MediatorLiveData<>();

    private final LiveData<List<Seance>> seancesForCurrentMatiere;

    private final LiveData<List<MatiereListAdapter.MatiereWithStats>> matieresWithStatsForCurrentUe;

    public UEViewModel(@NonNull Application application) {
        super(application);
        ueDao = new UEDAO(application);
        seanceDao = new SeanceDAO(application);
        matiereDao = new MatiereDAO(application);

        // Load all data initially
        refreshAllData();

        // Combine the three caches into a single LiveData that updates whenever any cache changes
        MediatorLiveData<CombinedData> combined = new MediatorLiveData<>();
        combined.addSource(listUEs, ues -> combineAndEmit(combined));
        combined.addSource(listMatieres, matieres -> combineAndEmit(combined));
        combined.addSource(listSeances, seances -> combineAndEmit(combined));

        currentMatiereWithStats.addSource(currentMatiereId, id -> updateCurrentMatiereStats());
        currentMatiereWithStats.addSource(listMatieres, matieres -> updateCurrentMatiereStats());
        currentMatiereWithStats.addSource(listSeances, seances -> updateCurrentMatiereStats());

        // Transform the combined data into the list of UeWithStats
        ueWithStatsList = Transformations.map(combined, data -> {
            if (data.ues == null) return Collections.emptyList();
            return computeUeWithStats(data.ues, data.matieres, data.seances);
        });

        statsGlobal = Transformations.map(ueWithStatsList, ues -> {
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

        currentUeWithStats = Transformations.switchMap(currentUeId, ueId -> {
            if (ueId == null || ueId == 0) {
                return new MutableLiveData<>(null);
            }
            // Transform the ueUiModels LiveData – this will update whenever ueUiModels changes
            return Transformations.map(ueWithStatsList, models -> {
                if (models == null) return null;
                for (UEListAdapter.UeWithStats model : models) {
                    if (model.ue().getId() == ueId) {
                        Log.d("MYAPP::UEViewModel", "map-currentUeId changed to " + ueId);
                        return model;
                    }
                }
                return null;
            });
        });

        matieresWithStatsForCurrentUe = Transformations.switchMap(currentUeId, ueId -> {
            if (ueId == null || ueId == 0) {
                return new MutableLiveData<>(Collections.emptyList());
            }
            // When currentUeId changes, observe listMatieres (the full list) and filter
            return Transformations.map(listMatieres, allMatieres -> {
                if (allMatieres == null) return Collections.emptyList();

                // Filter matières belonging to this UE
                List<Matiere> filteredMatieres = new ArrayList<>();
                for (Matiere m : allMatieres) {
                    if (m.getUeId() == ueId) {
                        filteredMatieres.add(m);
                    }
                }

                // Use current seances to compute stats per matière
                List<Seance> allSeances = listSeances.getValue();
                if (allSeances == null) allSeances = Collections.emptyList();

                Map<Integer, Integer> seanceSumByMatiere = new HashMap<>();
                for (Seance s : allSeances) {
                    int matId = s.getMatiereId();
                    Integer current = seanceSumByMatiere.get(matId);
                    if (current == null) current = 0;
                    seanceSumByMatiere.put(matId, current + s.getDuree());

                }

                List<MatiereListAdapter.MatiereWithStats> result = new ArrayList<>();
                for (Matiere m : filteredMatieres) {
                    int total = m.getVolumeHoraire();
                    Integer effectueObj = seanceSumByMatiere.get(m.getId());
                    int effectue = (effectueObj == null) ? 0 : effectueObj;
                    int percentage = total > 0 ? (effectue * 100) / total : 0;
                    String statText = String.format(Locale.getDefault(), "%dH / %dH", effectue, total);
                    result.add(new MatiereListAdapter.MatiereWithStats(m, statText, percentage));
                }
                return result;
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
            return; // not all data loaded yet
        }

        // Find the matiere
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

        // Sum seance durations for this matiere
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

    private List<UEListAdapter.UeWithStats> computeUeWithStats(List<UE> ues,
                                                               List<Matiere> matieres,
                                                               List<Seance> seances) {
        if (ues == null || matieres == null || seances == null) {
            return Collections.emptyList();
        }

        // 1. Total planned hours per UE (from matières)
        Map<Integer, Integer> uetotalVolumeHoraire = new HashMap<>();
        for (Matiere m : matieres) {
            int ueId = m.getUeId();
            Integer current = uetotalVolumeHoraire.get(ueId);
            if (current == null) current = 0;
            uetotalVolumeHoraire.put(ueId, current + m.getVolumeHoraire());
        }

        // 2. Map matière -> UE (to link seances to UE)
        Map<Integer, Integer> matiereToUe = new HashMap<>();
        for (Matiere m : matieres) {
            matiereToUe.put(m.getId(), m.getUeId());
        }

        // 3. Completed hours per UE (from seances)
        Map<Integer, Integer> ueVolumeHoraireEffecture = new HashMap<>();
        for (Seance s : seances) {
            Integer ueId = matiereToUe.get(s.getMatiereId());
            if (ueId != null) {
                Integer current = ueVolumeHoraireEffecture.get(ueId);
                if (current == null) current = 0;
                ueVolumeHoraireEffecture.put(ueId, current + s.getDuree());
            }
        }

        // 4. Build result list
        List<UEListAdapter.UeWithStats> result = new ArrayList<>();
        for (UE ue : ues) {
            int ueId = ue.getId();
            Integer volumeHoraire = uetotalVolumeHoraire.get(ueId);
            Integer horaireEffectue = ueVolumeHoraireEffecture.get(ueId);
            int total = (volumeHoraire == null) ? 0 : volumeHoraire;
            int effectue = (horaireEffectue == null) ? 0 : horaireEffectue;
            int percentage = (total > 0) ? (effectue * 100) / total : 0;
            String statText = String.format(Locale.getDefault(), "%dH dispensées / %dH", effectue, total);
            result.add(new UEListAdapter.UeWithStats(ue, statText, percentage));
        }
        return result;
    }

    public void addUE(UE ue) {
        ueDao.insert(ue, this::refreshAllData);
    }

    public void addMatiere(Matiere matiere, int ueId, Runnable onComplete) {
        matiereDao.insert(matiere, () -> {
            refreshAllData();
            // Ensure currentUeId is set to the same UE to trigger recomputation of stats
            currentUeId.postValue(ueId);
            if (onComplete != null) onComplete.run();
        });
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

    public void refreshAllData() {
        ueDao.getAll(listUEs::postValue);
        matiereDao.getAll(listMatieres::postValue);
        seanceDao.getAll(listSeances::postValue);
    }

    public void setCurrentUeId(int currentUeId) {
        this.currentUeId.setValue(currentUeId);
    }

    public void setCurrentMatiereId(int currentMatiereId) {
        this.currentMatiereId.setValue(currentMatiereId);
    }

    public LiveData<List<Matiere>> getListMatieres() {
        return listMatieres;
    }

    public LiveData<List<Seance>> getListSeances() {
        return listSeances;
    }

    public LiveData<List<UE>> getListUEs() {
        return listUEs;
    }

    public LiveData<StatsGlobal> getStatsGlobal() {
        return statsGlobal;
    }

    public LiveData<List<UEListAdapter.UeWithStats>> getUeWithStatsList() {
        return ueWithStatsList;
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
}