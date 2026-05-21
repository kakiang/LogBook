package com.hervekakiang.logbook.seance;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hervekakiang.logbook.db.DAOBase;
import com.hervekakiang.logbook.db.MyDatabaseHelper;
import com.hervekakiang.logbook.matiere.Matiere;

import java.util.ArrayList;
import java.util.List;

public class SeanceDAO extends DAOBase<Seance> {

    private final MutableLiveData<List<Seance>> listSeances = new MutableLiveData<>();

    public SeanceDAO(Context context) {
        super(context);
    }

    public long add(Seance seance) {
        ContentValues values = new ContentValues();
        values.put(MyDatabaseHelper.SEANCE_MATIERE_ID, seance.getMatiereId());
        values.put(MyDatabaseHelper.SEANCE_DATE, seance.getDate());
        values.put(MyDatabaseHelper.SEANCE_HEURE_DEBUT, seance.getHeureDebut());
        values.put(MyDatabaseHelper.SEANCE_DUREE, seance.getDuree());
        values.put(MyDatabaseHelper.SEANCE_CONTENU_PEDAGOGIQUE, seance.getContenuPedagogique());
        return myDb.insert(MyDatabaseHelper.TABLE_SEANCE, null, values);
    }

    public void insert(Seance seance, Runnable onComplete) {
        executorService.execute(() -> {
            add(seance);
            onComplete.run();
        });
    }

    public List<Seance> fetchAll() {
        List<Seance> seances = new ArrayList<>();
        try(Cursor cursor = myDb.query(MyDatabaseHelper.TABLE_SEANCE, null, null, null, null, null, MyDatabaseHelper.SEANCE_DATE + " DESC")) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.SEANCE_ID));
                int matiereId = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.SEANCE_MATIERE_ID));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.SEANCE_DATE));
                String heureDebut = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.SEANCE_HEURE_DEBUT));
                int duree = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.SEANCE_DUREE));
                String contenuPedagogique = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.SEANCE_CONTENU_PEDAGOGIQUE));
                seances.add(new Seance(id, matiereId, date, heureDebut, duree, contenuPedagogique));
            }
        } catch (Exception e) {
            Log.e("SeanceDAO", "Error fetching seances", e);
        }
        return seances;
    }

    public List<Seance> fetchByMatiereId(int matiereId) {
        List<Seance> seances = new ArrayList<>();
        try(Cursor cursor = myDb.query(MyDatabaseHelper.TABLE_SEANCE, null, MyDatabaseHelper.SEANCE_MATIERE_ID + " = ?", new String[]{String.valueOf(matiereId)}, null, null, MyDatabaseHelper.SEANCE_DATE + " DESC")) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.SEANCE_ID));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.SEANCE_DATE));
                String heureDebut = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.SEANCE_HEURE_DEBUT));
                int duree = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.SEANCE_DUREE));
                String contenuPedagogique = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.SEANCE_CONTENU_PEDAGOGIQUE));
                seances.add(new Seance(id, matiereId, date, heureDebut, duree, contenuPedagogique));
            }
        } catch (Exception e) {
            Log.e("SeanceDAO", "Error fetching seances", e);
        }
        return seances;
    }


    public void getAll(SeanceDAO.Callback<List<Seance>> callback){
        executorService.execute(() -> {
            List<Seance> ues = fetchAll();
            callback.onResult(ues);
        });
    }

    public LiveData<List<Seance>> getAll(){
        executorService.execute(() -> {
            List<Seance> list = fetchAll();
            listSeances.postValue(list);
        });

        return listSeances;
    }

    public LiveData<List<Seance>> getSeancesByMatiereId(int matiereId){
        executorService.execute(() -> {
            List<Seance> list = fetchByMatiereId(matiereId);
            listSeances.postValue(list);
        });
        return listSeances;
    }

    public int getTotalVolumeHoraireEffectueByUeId(int ueId) {
        String query ="SELECT SUM(s.duree) FROM seances s INNER JOIN matieres m ON s.matiere_id = m.id WHERE m.ue_id = ?";
        try(Cursor cursor = myDb.rawQuery(query, new String[]{String.valueOf(ueId)})){
            int totalVolumeHoraireEffectue = 0;
            if (cursor.moveToFirst()) {
                totalVolumeHoraireEffectue = cursor.getInt(0);
            }
            return totalVolumeHoraireEffectue;
        } catch (Exception e) {
            Log.e("SeanceDAO", "Error fetching total volume horaire effectué", e);
        }
        return 0;
    }

    public int getTotalVolumeHoraireEffectueByMatiereId(int matiereId) {
        String query ="SELECT SUM(s.duree) FROM seances s WHERE s.matiere_id = ?";
        try(Cursor cursor = myDb.rawQuery(query, new String[]{String.valueOf(matiereId)})){
            int totalVolumeHoraireEffectue = 0;
            if (cursor.moveToFirst()) {
                totalVolumeHoraireEffectue = cursor.getInt(0);
            }
            return totalVolumeHoraireEffectue;
        } catch (Exception e) {
            Log.e("SeanceDAO", "Error fetching total volume horaire effectué", e);
        }
        return 0;
    }

    public int getTotalVolumeHoraireEffectue() {
        String query ="SELECT SUM(s.duree) FROM seances s";
        try(Cursor cursor = myDb.rawQuery(query, null)){
            int totalVolumeHoraireEffectue = 0;
            if (cursor.moveToFirst()) {
                totalVolumeHoraireEffectue = cursor.getInt(0);
            }
            return totalVolumeHoraireEffectue;
        } catch (Exception e) {
            Log.e("SeanceDAO", "Error fetching total volume horaire effectué", e);
        }
        return 0;
    }


    public int update(Seance seance) {
        ContentValues values = new ContentValues();
        values.put(MyDatabaseHelper.SEANCE_MATIERE_ID, seance.getMatiereId());
        values.put(MyDatabaseHelper.SEANCE_DATE, seance.getDate());
        values.put(MyDatabaseHelper.SEANCE_HEURE_DEBUT, seance.getHeureDebut());
        values.put(MyDatabaseHelper.SEANCE_DUREE, seance.getDuree());
        values.put(MyDatabaseHelper.SEANCE_CONTENU_PEDAGOGIQUE, seance.getContenuPedagogique());
        return myDb.update(MyDatabaseHelper.TABLE_SEANCE, values, MyDatabaseHelper.SEANCE_ID + " = ?",
                new String[]{String.valueOf(seance.getId())});
    }

    public int delete(Seance seance) {
        return myDb.delete(MyDatabaseHelper.TABLE_SEANCE, MyDatabaseHelper.SEANCE_ID + " = ?",
                new String[]{String.valueOf(seance.getId())});
    }
}
