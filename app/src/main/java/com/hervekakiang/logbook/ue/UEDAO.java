package com.hervekakiang.logbook.ue;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.hervekakiang.logbook.db.DAOBase;
import com.hervekakiang.logbook.db.MyDatabaseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UEDAO extends DAOBase {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public UEDAO(Context context) {
        super(context);
    }

    public void add(UE ue) {
        open();
        ContentValues values = new ContentValues();
        values.put(MyDatabaseHelper.UE_CODE, ue.getCode());
        values.put(MyDatabaseHelper.UE_NOM, ue.getNom());
        myDb.insert(MyDatabaseHelper.TABLE_UE, null, values);
        close();
    }

    public String getNom(int id) {
        open();
        Cursor cursor = myDb.query(MyDatabaseHelper.TABLE_UE, null, MyDatabaseHelper.UE_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        String nom = "";
        if (cursor.moveToFirst()) {
            nom = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.UE_NOM));
        }
        cursor.close();
        close();
        return nom;
    }

    public void insert(UE ue, Runnable onComplete) {
        executorService.execute(() -> {
            add(ue);
            onComplete.run();
        });
    }

    private List<UE> fetchAll() {
        open();
        Cursor cursor = myDb.query(MyDatabaseHelper.TABLE_UE, null, null, null, null, null, "nom ASC");
        List<UE> ues = new ArrayList<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.UE_ID));
            String code = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.UE_CODE));
            String nom = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.UE_NOM));
            ues.add(new UE(id, code, nom));
        }
        cursor.close();
        close();
        return ues;
    }

    public void getAll(Callback<List<UE>> callback){
        executorService.execute(() -> {
            List<UE> ues = fetchAll();
            callback.onResult(ues);
        });
    }

    public void update(UE ue) {
        open();
        ContentValues values = new ContentValues();
        values.put(MyDatabaseHelper.UE_CODE, ue.getCode());
        values.put(MyDatabaseHelper.UE_NOM, ue.getNom());
        myDb.update(MyDatabaseHelper.TABLE_UE, values, MyDatabaseHelper.UE_ID + " = ?",
                new String[]{String.valueOf(ue.getId())});
        close();
    }

    public void delete(UE ue) {
        open();
        myDb.delete(MyDatabaseHelper.TABLE_UE, MyDatabaseHelper.UE_ID + " = ?",
                new String[]{String.valueOf(ue.getId())});
        close();
    }

    public interface Callback<T> {
        void onResult(T result);
    }
}
