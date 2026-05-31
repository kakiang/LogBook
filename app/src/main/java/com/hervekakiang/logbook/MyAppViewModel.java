package com.hervekakiang.logbook;

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
import com.hervekakiang.logbook.ue.UE;
import com.hervekakiang.logbook.ue.UEDAO;
import com.hervekakiang.logbook.ue.UEListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MyAppViewModel extends AndroidViewModel {
    private final UEDAO ueDao;
    private final SeanceDAO seanceDao;
    private final MatiereDAO matiereDao;

    private final MutableLiveData<Integer> currentUeId = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentMatiereId = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentSeanceId = new MutableLiveData<>();

    private final MutableLiveData<List<UE>> listUEs = new MutableLiveData<>();
    private final MutableLiveData<List<Matiere>> listMatieres = new MutableLiveData<>();
    private final MutableLiveData<List<Seance>> listSeances = new MutableLiveData<>();

    private final LiveData<List<Seance>> filteredSeances;
    private final LiveData<List<MatiereListAdapter.MatiereDTO>> filteredMatieres;

    private final MediatorLiveData<List<UEListAdapter.UEDTO>> listUEDTO = new MediatorLiveData<>();

    private final LiveData<StatsGlobal> statsGlobal;
    private final LiveData<UEListAdapter.UEDTO> currentUEDTO;
    private final LiveData<Map<String, String>> currentSeanceDTO;
    private final MediatorLiveData<MatiereListAdapter.MatiereDTO> currentMatiereDTO = new MediatorLiveData<>();

    private final LiveData<List<Seance>> listSeanceForCurrentMatiere;

    private final MediatorLiveData<List<MatiereListAdapter.MatiereDTO>> listMatiereDTO = new MediatorLiveData<>();
    private final MediatorLiveData<List<MatiereListAdapter.MatiereDTO>> listMatiereDTOForCurrentUE = new MediatorLiveData<>();

    private final MutableLiveData<Integer> pendingDeleteMatiereId = new MutableLiveData<>();
    private final MutableLiveData<Integer> pendingDeleteUeId = new MutableLiveData<>();

    private final MutableLiveData<String> seanceSearchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> matiereSearchQuery = new MutableLiveData<>("");

    public MyAppViewModel(@NonNull Application application) {
        super(application);
        ueDao = new UEDAO(application);
        seanceDao = new SeanceDAO(application);
        matiereDao = new MatiereDAO(application);

        refreshAllData();

        MediatorLiveData<AllData> allData = new MediatorLiveData<>();
        allData.addSource(listUEs, ues -> updateAllData(allData));
        allData.addSource(listMatieres, matieres -> updateAllData(allData));
        allData.addSource(listSeances, seances -> updateAllData(allData));

        statsGlobal = Transformations.map(listUEDTO, ues -> {
            int total = 0, effectue = 0;
            List<Matiere> allMatieres = listMatieres.getValue();
            if (allMatieres != null) {
                for (Matiere m : allMatieres) total += m.getVolumeHoraire();
            }
            List<Seance> allSeances = listSeances.getValue();
            if (allSeances != null) {
                for (Seance s : allSeances) effectue += s.getDuree();
            }
            return new StatsGlobal(total, effectue);
        });

        listUEDTO.addSource(allData, data -> prepareListUEDTO(data, pendingDeleteUeId.getValue()));
        listUEDTO.addSource(pendingDeleteUeId, hidden -> prepareListUEDTO(allData.getValue(), hidden));

        currentUEDTO = Transformations.switchMap(currentUeId, ueId -> {
            if (ueId == null || ueId == 0) {
                return new MutableLiveData<>(null);
            }

            return Transformations.map(listUEDTO, uedtos -> {
                if (uedtos == null) return null;
                for (UEListAdapter.UEDTO uedto : uedtos) {
                    if (uedto.ue().getId() == ueId) {
                        Log.d("MYAPP::MyAppViewModel", "currentUEDTO:currentUeId changed to " + ueId);
                        return uedto;
                    }
                }
                return null;
            });
        });

        listMatiereDTO.addSource(listMatieres, matieres -> prepareListMatiereDTO());
        listMatiereDTO.addSource(listSeances, seances -> prepareListMatiereDTO());

        currentMatiereDTO.addSource(currentMatiereId, id -> prepareCurrentMatiereDTO());
        currentMatiereDTO.addSource(listMatiereDTO, matieres -> prepareCurrentMatiereDTO());

        listMatiereDTOForCurrentUE.addSource(currentUeId, ueId ->
                prepareListMatiereDTOForSelectedUE(ueId, pendingDeleteMatiereId.getValue()));
        listMatiereDTOForCurrentUE.addSource(pendingDeleteMatiereId, hidden ->
                prepareListMatiereDTOForSelectedUE(currentUeId.getValue(), hidden));
        listMatiereDTOForCurrentUE.addSource(listMatiereDTO, matieres ->
                prepareListMatiereDTOForSelectedUE(currentUeId.getValue(), pendingDeleteMatiereId.getValue()));


        listSeanceForCurrentMatiere = Transformations.switchMap(currentMatiereId, matiereId -> {
            if (matiereId == null || matiereId == 0) {
                return new MutableLiveData<>(Collections.emptyList());
            }
            return Transformations.map(listSeances, allSeances -> {
                if (allSeances == null) return Collections.emptyList();
                List<Seance> seanceListForCurrentMatiere = new ArrayList<>();
                for (Seance s : allSeances) {
                    if (s.getMatiereId() == matiereId) {
                        seanceListForCurrentMatiere.add(s);
                    }
                }
                return seanceListForCurrentMatiere;
            });
        });

        currentSeanceDTO = Transformations.switchMap(currentSeanceId, seanceDao::getSeanceById);

        filteredSeances = Transformations.switchMap(seanceSearchQuery, searchQuery -> {
            if (searchQuery == null || searchQuery.trim().isEmpty()) {
                return listSeances;
            }

            return Transformations.map(listSeances, allSeances -> {
                if (allSeances == null) return Collections.emptyList();
                List<Seance> filtered = new ArrayList<>();
                String query = searchQuery.toLowerCase().trim();
                for (Seance s : allSeances) {
                    if (s.getContenuPedagogique().toLowerCase().contains(query)) {
                        filtered.add(s);
                    }
                }
                return filtered;
            });

        });

        filteredMatieres = Transformations.switchMap(matiereSearchQuery, searchQuery -> {
            if (searchQuery == null || searchQuery.trim().isEmpty()) {
                return listMatiereDTO;
            }

            return Transformations.map(listMatiereDTO, allMatieres -> {
                if (allMatieres == null) return Collections.emptyList();
                List<MatiereListAdapter.MatiereDTO> filtered = new ArrayList<>();
                String query = searchQuery.toLowerCase().trim();
                for (MatiereListAdapter.MatiereDTO m : allMatieres) {
                    if (m.matiere().getNom().toLowerCase().contains(query)
                            || m.matiere().getEnseignant().toLowerCase().contains(query)
                            || m.volumeHoraireStat().toLowerCase().contains(query)
                            || String.valueOf(m.pourcentage()).toLowerCase().contains(query)) {
                        filtered.add(m);
                    }
                }
                return filtered;
            });

        });

    }

    private void updateAllData(MediatorLiveData<AllData> data) {
        List<UE> ues = listUEs.getValue();
        List<Matiere> matieres = listMatieres.getValue();
        List<Seance> seances = listSeances.getValue();
        if (ues == null || matieres == null || seances == null) {
            return;
        }
        data.setValue(new AllData(ues, matieres, seances));
    }

    private void prepareListUEDTO(AllData allData, Integer hidden) {

        if (allData == null) {
            listUEDTO.postValue(Collections.emptyList());
            return;
        }

        List<UE> ues = allData.ues();
        List<Matiere> matieres = allData.matieres();
        List<Seance> seances = allData.seances();

        if (ues == null || matieres == null || seances == null) {
            listUEDTO.postValue(Collections.emptyList());
            return;
        }

        Map<Integer, Integer> vhTotalParUE = calculVhTotalParUE(matieres);
        Map<Integer, Integer> vhDispenseParUE = calculVhDispenseParUE(matieres, seances);

        List<UEListAdapter.UEDTO> result = new ArrayList<>();
        for (UE ue : ues) {
            if (hidden != null && ue.getId() == hidden) continue;
            Integer vhTotal = vhTotalParUE.get(ue.getId());
            Integer vhDispense = vhDispenseParUE.get(ue.getId());
            StatInfo statInfo = new StatInfo(vhTotal, vhDispense);
            result.add(new UEListAdapter.UEDTO(ue, statInfo.ratioText, statInfo.pourcentage));
        }
        listUEDTO.postValue(result);
    }

    private void prepareListMatiereDTO() {

        if (listMatieres.getValue() == null
                || listMatieres.getValue().isEmpty()) {
            listMatiereDTO.setValue(Collections.emptyList());
            return;
        }

        listMatiereDTO.setValue(Collections.emptyList());

        List<Matiere> allMatieres = listMatieres.getValue();
        Map<Integer, Integer> vhDispenseParMatiere = calculVhDispenseParMatiere(listSeances.getValue());
        List<MatiereListAdapter.MatiereDTO> result = new ArrayList<>();

        for (Matiere m : allMatieres) {
            int vhTotal = m.getVolumeHoraire();
            Integer vhDispense = vhDispenseParMatiere.get(m.getId());
            StatInfo statInfo = new StatInfo(vhTotal, vhDispense);
            result.add(new MatiereListAdapter.MatiereDTO(m, statInfo.ratioText, statInfo.pourcentage));
        }
        listMatiereDTO.postValue(result);
    }

    private void prepareListMatiereDTOForSelectedUE(Integer ueId, Integer hidden) {

        if (ueId == null || ueId == 0
                || listMatieres.getValue() == null
                || listMatieres.getValue().isEmpty()) {
            listMatiereDTOForCurrentUE.setValue(Collections.emptyList());
            return;
        }

        List<MatiereListAdapter.MatiereDTO> result = new ArrayList<>();

        List<MatiereListAdapter.MatiereDTO> matiereDTOS = listMatiereDTO.getValue();
        if (matiereDTOS == null) return;
        listMatiereDTOForCurrentUE.setValue(Collections.emptyList());
        for (MatiereListAdapter.MatiereDTO matiereDTO : matiereDTOS) {
            if (matiereDTO.matiere().getUeId() == ueId){
                if(hidden != null && matiereDTO.matiere().getId() == hidden) continue;
                result.add(matiereDTO);
            }
        }

        listMatiereDTOForCurrentUE.postValue(result);
    }

    private void prepareCurrentMatiereDTO() {
        Integer matiereId = currentMatiereId.getValue();
        if (matiereId == null || matiereId == 0) {
            currentMatiereDTO.setValue(null);
            return;
        }
        List<MatiereListAdapter.MatiereDTO> matiereDTOS = listMatiereDTO.getValue();
        if (matiereDTOS == null) return;

        MatiereListAdapter.MatiereDTO matiereDTO = null;
        for (MatiereListAdapter.MatiereDTO m : matiereDTOS) {
            if (m.matiere().getId() == matiereId) {
                matiereDTO = m;
                break;
            }
        }
        if (matiereDTO == null) {
            currentMatiereDTO.setValue(null);
            return;
        }
        currentMatiereDTO.setValue(matiereDTO);
    }

    private Map<Integer, Integer> calculVhDispenseParMatiere(List<Seance> seances) {
        Map<Integer, Integer> map = new HashMap<>();
        if (seances == null) return map;
        for (Seance s : seances) {
            int matId = s.getMatiereId();
            Integer current = map.get(matId);
            if (current == null) current = 0;
            map.put(matId, current + s.getDuree());
        }
        return map;
    }

    private Map<Integer, Integer> calculVhDispenseParUE(List<Matiere> matieres, List<Seance> seances) {
        Map<Integer, Integer> matiereToUe = new HashMap<>();
        for (Matiere m : matieres) {
            matiereToUe.put(m.getId(), m.getUeId());
        }
        Map<Integer, Integer> vhDispenseParUE = new HashMap<>();
        if (seances == null) return vhDispenseParUE;
        for (Seance s : seances) {
            Integer ueId = matiereToUe.get(s.getMatiereId());
            if (ueId != null) {
                Integer vhDispense = vhDispenseParUE.get(ueId);
                if (vhDispense == null) vhDispense = 0;
                vhDispenseParUE.put(ueId, vhDispense + s.getDuree());
            }
        }
        return vhDispenseParUE;
    }

    private Map<Integer, Integer> calculVhTotalParUE(List<Matiere> matieres) {
        Map<Integer, Integer> vhTotalParUE = new HashMap<>();
        for (Matiere m : matieres) {
            int ueId = m.getUeId();
            Integer vh = vhTotalParUE.get(ueId);
            if (vh == null) vh = 0;
            vhTotalParUE.put(ueId, vh + m.getVolumeHoraire());
        }
        return vhTotalParUE;
    }

    private record AllData(List<UE> ues, List<Matiere> matieres, List<Seance> seances) {
    }

    public record StatsGlobal(int total, int effectue) {
    }

    private static class StatInfo {
        final int pourcentage;
        final String ratioText;

        StatInfo(Integer vhTotal, Integer vhDispense) {
            int total = (vhTotal == null) ? 0 : vhTotal;
            int effectue = (vhDispense == null) ? 0 : vhDispense;
            pourcentage = (total > 0) ? (effectue * 100) / total : 0;
            ratioText = String.format(Locale.getDefault(), "%dH / %dH", effectue, total);
        }
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

    public void addSeance(Seance seance, SeanceDAO.OnEntryInsertedListener onComplete) {
        seanceDao.insert(seance, (newId) -> {
            refreshAllData();
            Integer currentId = currentMatiereId.getValue();
            if (currentId != null) {
                currentMatiereId.postValue(currentId);
            }
            if (onComplete != null) onComplete.onInserted(newId);
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

    public void setSeanceSearchQuery(String seanceSearchQuery) {
        this.seanceSearchQuery.setValue(seanceSearchQuery);
    }

    public void setMatiereSearchQuery(String matiereSearchQuery) {
        this.matiereSearchQuery.setValue(matiereSearchQuery);
    }

    public LiveData<List<Seance>> getListSeances() {
        return listSeances;
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

    public LiveData<List<UEListAdapter.UEDTO>> getListUEDTO() {
        return listUEDTO;
    }

    public LiveData<UEListAdapter.UEDTO> getCurrentUEDTO() {
        return currentUEDTO;
    }

    public LiveData<List<MatiereListAdapter.MatiereDTO>> getListMatiereDTOForCurrentUE() {
        return listMatiereDTOForCurrentUE;
    }

    public LiveData<List<Seance>> getListSeanceForCurrentMatiere() {
        return listSeanceForCurrentMatiere;
    }

    public LiveData<MatiereListAdapter.MatiereDTO> getCurrentMatiereDTO() {
        return currentMatiereDTO;
    }

    public LiveData<Map<String, String>> getCurrentSeanceDTO() {
        return currentSeanceDTO;
    }

    public LiveData<List<Seance>> getFilteredSeances() {
        return filteredSeances;
    }

    public LiveData<List<MatiereListAdapter.MatiereDTO>> getFilteredMatieres() {
        return filteredMatieres;
    }
}