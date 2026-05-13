package com.hervekakiang.logbook.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    public static final String TABLE_UE = "ues";
    public static final String TABLE_MATIERE = "matieres";
    public static final String TABLE_SEANCE = "seances";

    public static final String UE_ID = "id";
    public static final String UE_CODE = "code";
    public static final String UE_NOM = "nom";

    public static final String MATIERE_ID = "id";
    public static final String MATIERE_UE_ID = "ue_id";
    public static final String MATIERE_NOM = "nom";
    public static final String MATIERE_ENSEIGNANT = "enseignant";
    public static final String MATIERE_VOLUME_HORAIRE = "volume_horaire";


    public static final String SEANCE_ID = "id";
    public static final String SEANCE_MATIERE_ID = "matiere_id";
    public static final String SEANCE_DATE = "date";
    public static final String SEANCE_HEURE_DEBUT = "heure_debut";
    public static final String SEANCE_DUREE = "duree";
    public static final String SEANCE_CONTENU_PEDAGOGIQUE = "contenu_pedagogique";

    public MyDatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_UE = "CREATE TABLE " + TABLE_UE + "("
                + UE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + UE_CODE + " TEXT,"
                + UE_NOM + " TEXT)";

        String CREATE_TABLE_MATIERE = "CREATE TABLE " + TABLE_MATIERE + "("
                + MATIERE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MATIERE_UE_ID + " INTEGER,"
                + MATIERE_NOM + " TEXT,"
                + MATIERE_ENSEIGNANT + " TEXT,"
                + MATIERE_VOLUME_HORAIRE + " INTEGER,"
                + "FOREIGN KEY(" + MATIERE_UE_ID + ") REFERENCES " + TABLE_UE + "(" + UE_ID + "))";

        String CREATE_TABLE_SEANCE = "CREATE TABLE " + TABLE_SEANCE + "("
                + SEANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SEANCE_MATIERE_ID + " INTEGER,"
                + SEANCE_DATE + " TEXT,"
                + SEANCE_HEURE_DEBUT + " TEXT,"
                + SEANCE_DUREE + " INTEGER,"
                + SEANCE_CONTENU_PEDAGOGIQUE + " TEXT,"
                + "FOREIGN KEY(" + SEANCE_MATIERE_ID + ") REFERENCES " + TABLE_MATIERE + "(" + MATIERE_ID + "))";

        db.execSQL(CREATE_TABLE_UE);
        db.execSQL(CREATE_TABLE_MATIERE);
        db.execSQL(CREATE_TABLE_SEANCE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_UE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MATIERE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEANCE);
        onCreate(db);
    }
}
