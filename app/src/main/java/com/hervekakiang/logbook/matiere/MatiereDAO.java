package com.hervekakiang.logbook.matiere;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hervekakiang.logbook.db.DAOBase;
import com.hervekakiang.logbook.db.MyDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class MatiereDAO extends DAOBase<Matiere> {

    public MatiereDAO(Context context) {
        super(context);
    }

    public void add(Matiere matiere) {
        ContentValues values = new ContentValues();
        values.put(MyDatabaseHelper.MATIERE_UE_ID, matiere.getUeId());
        values.put(MyDatabaseHelper.MATIERE_NOM, matiere.getNom());
        values.put(MyDatabaseHelper.MATIERE_ENSEIGNANT, matiere.getEnseignant());
        values.put(MyDatabaseHelper.MATIERE_VOLUME_HORAIRE, matiere.getVolumeHoraire());
        myDb.insert(MyDatabaseHelper.TABLE_MATIERE, null, values);
    }

    public void insert(Matiere matiere, Runnable onComplete) {
        executorService.execute(() -> {
            add(matiere);
            onComplete.run();
        });
    }

    private void modify(Matiere matiere) {
        ContentValues values = new ContentValues();
        values.put(MyDatabaseHelper.MATIERE_UE_ID, matiere.getUeId());
        values.put(MyDatabaseHelper.MATIERE_NOM, matiere.getNom());
        values.put(MyDatabaseHelper.MATIERE_ENSEIGNANT, matiere.getEnseignant());
        values.put(MyDatabaseHelper.MATIERE_VOLUME_HORAIRE, matiere.getVolumeHoraire());
        myDb.update(MyDatabaseHelper.TABLE_MATIERE, values, MyDatabaseHelper.MATIERE_ID + " = ?",
                new String[]{String.valueOf(matiere.getId())});
    }

    public void update(Matiere matiere, Runnable onComplete) {
        executorService.execute(() -> {
            modify(matiere);
            onComplete.run();
        });
    }



    public List<Matiere> fetchAll() {
        List<Matiere> matieres = new ArrayList<>();
        try (Cursor cursor = myDb.query(MyDatabaseHelper.TABLE_MATIERE, null, null, null, null, null, null)) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.MATIERE_ID));
                int ueId = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.MATIERE_UE_ID));
                String nom = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.MATIERE_NOM));
                String enseignant = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.MATIERE_ENSEIGNANT));
                int volumeHoraire = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.MATIERE_VOLUME_HORAIRE));
                matieres.add(new Matiere(id, ueId, nom, enseignant, volumeHoraire));
            }
        } catch (Exception e) {
            Log.e("MatiereDAO", "Error fetching matieres", e);
        }
        return matieres;
    }

    public void getAll(MatiereDAO.Callback<List<Matiere>> callback) {
        executorService.execute(() -> {
            List<Matiere> m = fetchAll();
            callback.onResult(m);
        });
    }

    private Matiere fetchById(int matiereId) {
        try (Cursor cursor = myDb.query(MyDatabaseHelper.TABLE_MATIERE, null, MyDatabaseHelper.MATIERE_ID + " = ?", new String[]{String.valueOf(matiereId)}, null, null, null)) {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.MATIERE_ID));
                int ueId = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.MATIERE_UE_ID));
                String nom = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.MATIERE_NOM));
                String enseignant = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.MATIERE_ENSEIGNANT));
                int volumeHoraire = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.MATIERE_VOLUME_HORAIRE));
                return new Matiere(id, ueId, nom, enseignant, volumeHoraire);
            }
        } catch (Exception e) {
            Log.e("MatiereDAO", "Error fetching matiere", e);
        }
        return null;
    }

    public LiveData<Matiere> getMatiereById(int matiereId) {
        MutableLiveData<Matiere> matiere = new MutableLiveData<>();
        executorService.execute(() -> {
            Matiere m = fetchById(matiereId);
            matiere.postValue(m);
        });
        return matiere;
    }


    public void delete(int matiereId, Runnable onComplete) {
        executorService.execute(() -> myDb.delete(MyDatabaseHelper.TABLE_MATIERE, MyDatabaseHelper.MATIERE_ID + " = ?",
                new String[]{String.valueOf(matiereId)}));
        if (onComplete != null) onComplete.run();
    }
}
