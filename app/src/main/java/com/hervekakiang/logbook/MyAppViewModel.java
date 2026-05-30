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

        MediatorLiveData<CombinedData> combined = new MediatorLiveData<>();
        combined.addSource(listUEs, ues -> combineAndEmit(combined));
        combined.addSource(listMatieres, matieres -> combineAndEmit(combined));
        combined.addSource(listSeances, seances -> combineAndEmit(combined));

        listUEDTO.addSource(combined, data -> prepareListUEDTO(data, pendingDeleteUeId.getValue()));
        listUEDTO.addSource(pendingDeleteUeId, hidden -> prepareListUEDTO(combined.getValue(), hidden));

        statsGlobal = Transformations.map(listUEDTO, ues -> {
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

        currentMatiereDTO.addSource(currentMatiereId, id -> prepareCurrentMatiereDTO());
        currentMatiereDTO.addSource(listMatieres, matieres -> prepareCurrentMatiereDTO());
        currentMatiereDTO.addSource(listSeances, seances -> prepareCurrentMatiereDTO());

        listMatiereDTO.addSource(listMatieres, matieres -> prepareListMatiereDTO());
        listMatiereDTO.addSource(listSeances, seances -> prepareListMatiereDTO());

        listMatiereDTOForCurrentUE.addSource(currentUeId, ueId ->
                prepareListMatiereDTOForSelectedUE(ueId, pendingDeleteMatiereId.getValue()));
        listMatiereDTOForCurrentUE.addSource(pendingDeleteMatiereId, hidden ->
                prepareListMatiereDTOForSelectedUE(currentUeId.getValue(), hidden));
        listMatiereDTOForCurrentUE.addSource(listMatieres, matieres ->
                prepareListMatiereDTOForSelectedUE(currentUeId.getValue(), pendingDeleteMatiereId.getValue()));

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

        listSeanceForCurrentMatiere = Transformations.switchMap(currentMatiereId, matiereId -> {
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

    private void combineAndEmit(MediatorLiveData<CombinedData> combined) {
        List<UE> ues = listUEs.getValue();
        List<Matiere> matieres = listMatieres.getValue();
        List<Seance> seances = listSeances.getValue();
        if (ues == null || matieres == null || seances == null) {
            return;
        }
        combined.setValue(new CombinedData(ues, matieres, seances));
    }

    private void prepareListMatiereDTO() {

        if (listMatieres.getValue() == null
                || listMatieres.getValue().isEmpty()) {
            listMatiereDTO.setValue(Collections.emptyList());
            return;
        }

        listMatiereDTO.setValue(Collections.emptyList());

        List<Matiere> filteredMatieres = listMatieres.getValue();

        List<Seance> allSeances = listSeances.getValue();
        if (allSeances == null) allSeances = Collections.emptyList();

        Map<Integer, Integer> volumeHoraireDispenseParMatiere = new HashMap<>();
        for (Seance s : allSeances) {
            int matId = s.getMatiereId();
            Integer current = volumeHoraireDispenseParMatiere.get(matId);
            if (current == null) current = 0;
            volumeHoraireDispenseParMatiere.put(matId, current + s.getDuree());
        }

        List<MatiereListAdapter.MatiereDTO> result = new ArrayList<>();
        for (Matiere m : filteredMatieres) {
            int vhTotal = m.getVolumeHoraire();
            Integer vhDispense = volumeHoraireDispenseParMatiere.get(m.getId());
            int dispense = (vhDispense == null) ? 0 : vhDispense;
            int percentage = vhTotal > 0 ? (dispense * 100) / vhTotal : 0;
            String statText = String.format(Locale.getDefault(), "%dH / %dH", dispense, vhTotal);
            result.add(new MatiereListAdapter.MatiereDTO(m, statText, percentage));
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

        listMatiereDTOForCurrentUE.setValue(Collections.emptyList());

        List<Matiere> filteredMatieres = new ArrayList<>();
        for (Matiere m : listMatieres.getValue()) {
            if (m.getUeId() == ueId) {
                if (hidden != null && m.getId() == hidden) continue;
                filteredMatieres.add(m);
            }
        }

        List<Seance> allSeances = listSeances.getValue();
        if (allSeances == null) allSeances = Collections.emptyList();

        Map<Integer, Integer> volumeHoraireDispenseParMatiere = new HashMap<>();
        for (Seance s : allSeances) {
            int matId = s.getMatiereId();
            Integer current = volumeHoraireDispenseParMatiere.get(matId);
            if (current == null) current = 0;
            volumeHoraireDispenseParMatiere.put(matId, current + s.getDuree());
        }

        List<MatiereListAdapter.MatiereDTO> result = new ArrayList<>();
        for (Matiere m : filteredMatieres) {
            int vhTotal = m.getVolumeHoraire();
            Integer vhDispense = volumeHoraireDispenseParMatiere.get(m.getId());
            int dispense = (vhDispense == null) ? 0 : vhDispense;
            int percentage = vhTotal > 0 ? (dispense * 100) / vhTotal : 0;
            String statText = String.format(Locale.getDefault(), "%dH / %dH", dispense, vhTotal);
            result.add(new MatiereListAdapter.MatiereDTO(m, statText, percentage));
        }
        listMatiereDTOForCurrentUE.postValue(result);
    }

    private void prepareCurrentMatiereDTO() {
        Integer matiereId = currentMatiereId.getValue();
        if (matiereId == null || matiereId == 0) {
            currentMatiereDTO.setValue(null);
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
            currentMatiereDTO.setValue(null);
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
        currentMatiereDTO.setValue(new MatiereListAdapter.MatiereDTO(matiere, statText, percentage));
    }

    private void prepareListUEDTO(CombinedData combinedData, Integer hidden) {

        if (combinedData == null) {
            listUEDTO.postValue(Collections.emptyList());
            return;
        }

        List<UE> ues = combinedData.ues();
        List<Matiere> matieres = combinedData.matieres();
        List<Seance> seances = combinedData.seances();

        if (ues == null || matieres == null || seances == null) {
            listUEDTO.postValue(Collections.emptyList());
            return;
        }

        Map<Integer, Integer> volumeHoraireParUE = new HashMap<>();
        for (Matiere m : matieres) {
            int ueId = m.getUeId();
            Integer vh = volumeHoraireParUE.get(ueId);
            if (vh == null) vh = 0;
            volumeHoraireParUE.put(ueId, vh + m.getVolumeHoraire());
        }

        Map<Integer, Integer> matiereToUe = new HashMap<>();
        for (Matiere m : matieres) {
            matiereToUe.put(m.getId(), m.getUeId());
        }

        Map<Integer, Integer> volumeHoraireDispenseParUE = new HashMap<>();
        for (Seance s : seances) {
            Integer ueId = matiereToUe.get(s.getMatiereId());
            if (ueId != null) {
                Integer vhDispense = volumeHoraireDispenseParUE.get(ueId);
                if (vhDispense == null) vhDispense = 0;
                volumeHoraireDispenseParUE.put(ueId, vhDispense + s.getDuree());
            }
        }

        List<UEListAdapter.UEDTO> result = new ArrayList<>();
        for (UE ue : ues) {
            if (hidden != null && ue.getId() == hidden) continue;
            int ueId = ue.getId();

            Integer volumeHoraire = volumeHoraireParUE.get(ueId);
            Integer vhDispense = volumeHoraireDispenseParUE.get(ueId);

            int total = (volumeHoraire == null) ? 0 : volumeHoraire;
            int effectue = (vhDispense == null) ? 0 : vhDispense;
            int percentage = (total > 0) ? (effectue * 100) / total : 0;

            String statText = String.format(Locale.getDefault(), "%dH dispensées / %dH", effectue, total);
            result.add(new UEListAdapter.UEDTO(ue, statText, percentage));
        }
        listUEDTO.postValue(result);
    }

    private record CombinedData(List<UE> ues, List<Matiere> matieres, List<Seance> seances) {
    }

    public record StatsGlobal(int total, int effectue) {
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

    private List<Seance> seancesWithNomMatiere(List<Seance> seances) {
        if (seances == null) return Collections.emptyList();
        List<Matiere> matieres = listMatieres.getValue();
        if (matieres == null) return Collections.emptyList();
        Map<Integer, String> matiereNoms = new HashMap<>();
        for (Matiere m : matieres) {
            matiereNoms.put(m.getId(), m.getNom());
        }

        for (Seance s : seances) {
            s.setContenuPedagogique(matiereNoms.get(s.getMatiereId()) + " - " + s.getContenuPedagogique());
        }
        return seances;
    }
}