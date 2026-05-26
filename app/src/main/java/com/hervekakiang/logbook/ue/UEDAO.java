package com.hervekakiang.logbook.ue;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.hervekakiang.logbook.db.DAOBase;
import com.hervekakiang.logbook.db.MyDatabaseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UEDAO extends DAOBase<UE> {

    public UEDAO(Context context) {
        super(context);
    }

    public void add(UE ue) {
        ContentValues values = new ContentValues();
        values.put(MyDatabaseHelper.UE_CODE, ue.getCode());
        values.put(MyDatabaseHelper.UE_NOM, ue.getNom());
        myDb.insert(MyDatabaseHelper.TABLE_UE, null, values);
    }

    public void insert(UE ue, Runnable onComplete) {
        executorService.execute(() -> {
            add(ue);
            onComplete.run();
        });
    }

    private List<UE> fetchAll() {
        List<UE> ues = new ArrayList<>();

       try(Cursor cursor = myDb.query(MyDatabaseHelper.TABLE_UE, null, null, null, null, null, "nom ASC")) {
           while (cursor.moveToNext()) {
               int id = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.UE_ID));
               String code = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.UE_CODE));
               String nom = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.UE_NOM));
               ues.add(new UE(id, code, nom));
           }
       } catch (Exception e) {
           Log.e("UEDAO", "Error fetching ues", e);
       }
        return ues;
    }

    public void getAll(Callback<List<UE>> callback){
        executorService.execute(() -> {
            List<UE> ues = fetchAll();
            callback.onResult(ues);
        });
    }

    public UE getUeById(int ueId){
        try(Cursor cursor = myDb.query(MyDatabaseHelper.TABLE_UE, null, MyDatabaseHelper.UE_ID + " = ?", new String[]{String.valueOf(ueId)}, null, null, null)) {
            if (cursor.moveToFirst()) {
                String code = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.UE_CODE));
                String nom = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.UE_NOM));
                return new UE(ueId, code, nom);
            }
        } catch (Exception e) {
            Log.e("UEDAO", "Error fetching ue", e);
        }
        return null;
    }

    public int getVolumeHoraire(UE ue) {
        int volumeHoraire = 0;
        try (Cursor cursor = myDb.rawQuery("SELECT SUM(" + MyDatabaseHelper.MATIERE_VOLUME_HORAIRE + ") FROM "
                        + MyDatabaseHelper.TABLE_MATIERE + " WHERE " + MyDatabaseHelper.MATIERE_UE_ID + " = ?",
                new String[]{String.valueOf(ue.getId())})){

            if (cursor.moveToFirst()) {
                volumeHoraire = cursor.getInt(0);
            }

        } catch (Exception e) {
            Log.e("UEDAO", "Error fetching volume horaire", e);
        }
        return volumeHoraire;
    }

    public void getCombine(){
        String query = """
                SELECT\s
                    ue.id AS ueId,\s
                    ue.nom AS ueNom,
                    SUM(s.duration) AS horaireEffectue,
                    SUM(m.volume_horaire) AS horaireTotal
                FROM UE ue
                LEFT JOIN Subject sub ON sub.ue_id = ue.id
                LEFT JOIN Session s ON s.subject_id = sub.id
                LEFT JOIN Matiere m ON m.ue_id = ue.id
                GROUP BY ue.id;
                """;
    }

    private void modify(UE ue) {
        ContentValues values = new ContentValues();
        values.put(MyDatabaseHelper.UE_CODE, ue.getCode());
        values.put(MyDatabaseHelper.UE_NOM, ue.getNom());
        myDb.update(MyDatabaseHelper.TABLE_UE, values, MyDatabaseHelper.UE_ID + " = ?",
                new String[]{String.valueOf(ue.getId())});
    }

    public void update(UE ue, Runnable onComplete) {
        executorService.execute(() -> {
            modify(ue);
            onComplete.run();
        });
    }

    public void delete(int ueId, Runnable onComplete) {
        executorService.execute(() -> myDb.delete(MyDatabaseHelper.TABLE_UE, MyDatabaseHelper.UE_ID + " = ?",
                new String[]{String.valueOf(ueId)}));
        if (onComplete!=null) onComplete.run();
    }
}
