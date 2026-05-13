package com.hervekakiang.logbook.matiere;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.hervekakiang.logbook.db.DAOBase;
import com.hervekakiang.logbook.db.MyDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class MatiereDAO extends DAOBase {

    public MatiereDAO(Context context) {
        super(context);
    }

    public long insert(Matiere matiere) {
        ContentValues values = new ContentValues();
        values.put(MyDatabaseHelper.MATIERE_UE_ID, matiere.getUeId());
        values.put(MyDatabaseHelper.MATIERE_NOM, matiere.getNom());
        values.put(MyDatabaseHelper.MATIERE_ENSEIGNANT, matiere.getEnseignant());
        values.put(MyDatabaseHelper.MATIERE_VOLUME_HORAIRE, matiere.getVolumeHoraire());
        return myDb.insert(MyDatabaseHelper.TABLE_MATIERE, null, values);
    }

    public List<Matiere> getAllMatieres() {
        Cursor cursor = myDb.query(MyDatabaseHelper.TABLE_MATIERE, null, null, null, null, null, null);
        List<Matiere> matieres = new ArrayList<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.MATIERE_ID));
            int ueId = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.MATIERE_UE_ID));
            String nom = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.MATIERE_NOM));
            String enseignant = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.MATIERE_ENSEIGNANT));
            int volumeHoraire = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.MATIERE_VOLUME_HORAIRE));
            matieres.add(new Matiere(id, ueId, nom, enseignant, volumeHoraire));
        }
        cursor.close();
        return matieres;
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
