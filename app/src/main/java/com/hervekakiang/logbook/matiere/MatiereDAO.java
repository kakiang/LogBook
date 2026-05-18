package com.hervekakiang.logbook.matiere;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.hervekakiang.logbook.db.DAOBase;
import com.hervekakiang.logbook.db.MyDatabaseHelper;
import com.hervekakiang.logbook.ue.UE;
import com.hervekakiang.logbook.ue.UEDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    public List<Matiere>  fetchByUeId(int ueId){
        List<Matiere> matieres = new ArrayList<>();
        try(Cursor cursor = myDb.query(MyDatabaseHelper.TABLE_MATIERE, null, MyDatabaseHelper.MATIERE_UE_ID + " = ?", new String[]{String.valueOf(ueId)}, null, null, null)) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.MATIERE_ID));
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

    public void getAll(MatiereDAO.Callback<List<Matiere>> callback){
        executorService.execute(() -> {
            List<Matiere> ues = fetchAll();
            callback.onResult(ues);
        });
    }

    public void getMatieresByUeId(int ueId, MatiereDAO.Callback<List<Matiere>> callback){
        executorService.execute(() -> {
            List<Matiere> ues = fetchByUeId(ueId);
            callback.onResult(ues);
        });
    }

    public int getTotalVolumeHoraireByUeId(int ueId) {
        String query ="SELECT SUM(m.volume_horaire) FROM matieres m INNER JOIN ues u ON m.ue_id = u.id WHERE u.id = ?";
        try(Cursor cursor = myDb.rawQuery(query, new String[]{String.valueOf(ueId)})){
            int totalVolumeHoraire = 0;
            if (cursor.moveToFirst()) {
                totalVolumeHoraire = cursor.getInt(0);
            }
            return totalVolumeHoraire;
        } catch (Exception e) {
            Log.e("MatiereDAO", "Error fetching total volume horaire", e);
        }
        return 0;
    }

    public int update(Matiere matiere) {
        ContentValues values = new ContentValues();
        values.put(MyDatabaseHelper.MATIERE_UE_ID, matiere.getUeId());
        values.put(MyDatabaseHelper.MATIERE_NOM, matiere.getNom());
        values.put(MyDatabaseHelper.MATIERE_ENSEIGNANT, matiere.getEnseignant());
        values.put(MyDatabaseHelper.MATIERE_VOLUME_HORAIRE, matiere.getVolumeHoraire());
        return myDb.update(MyDatabaseHelper.TABLE_MATIERE, values, MyDatabaseHelper.MATIERE_ID + " = ?",
                new String[]{String.valueOf(matiere.getId())});
    }

    public int delete(Matiere matiere) {
        return myDb.delete(MyDatabaseHelper.TABLE_MATIERE, MyDatabaseHelper.MATIERE_ID + " = ?",
                new String[]{String.valueOf(matiere.getId())});
    }
}
