package com.hervekakiang.logbook.seance;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.hervekakiang.logbook.db.DAOBase;
import com.hervekakiang.logbook.db.MyDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class SeanceDAO extends DAOBase {

    public SeanceDAO(Context context) {
        super(context);
    }

    public long insert(Seance seance) {
        ContentValues values = new ContentValues();
        values.put(MyDatabaseHelper.SEANCE_MATIERE_ID, seance.getMatiereId());
        values.put(MyDatabaseHelper.SEANCE_DATE, seance.getDate());
        values.put(MyDatabaseHelper.SEANCE_HEURE_DEBUT, seance.getHeureDebut());
        values.put(MyDatabaseHelper.SEANCE_DUREE, seance.getDuree());
        values.put(MyDatabaseHelper.SEANCE_CONTENU_PEDAGOGIQUE, seance.getContenuPedagogique());
        return myDb.insert(MyDatabaseHelper.TABLE_SEANCE, null, values);
    }

    public List<Seance> getAllSeances() {
        Cursor cursor = myDb.query(MyDatabaseHelper.TABLE_SEANCE, null, null, null, null, null, null);
        List<Seance> seances = new ArrayList<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.SEANCE_ID));
            int matiereId = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.SEANCE_MATIERE_ID));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.SEANCE_DATE));
            String heureDebut = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.SEANCE_HEURE_DEBUT));
            int duree = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.SEANCE_DUREE));
            String contenuPedagogique = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.SEANCE_CONTENU_PEDAGOGIQUE));
            seances.add(new Seance(id, matiereId, date, heureDebut, duree, contenuPedagogique));
        }
        cursor.close();
        return seances;
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
