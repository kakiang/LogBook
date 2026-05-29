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

    private static MyDatabaseHelper instance;
    private SQLiteDatabase database;

    public MyDatabaseHelper(@Nullable Context context, @Nullable String dbname, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context != null ? context.getApplicationContext() : null, dbname, factory, version);
    }

    public static synchronized MyDatabaseHelper getInstance(Context context, String dbname, int version) {
        if (instance == null) {
            instance = new MyDatabaseHelper(context, dbname, null, version);
        }
        return instance;
    }

    public synchronized SQLiteDatabase openDatabase() {
        if (database == null || !database.isOpen()) {
            database = this.getWritableDatabase();
        }
        return database;
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
                + "FOREIGN KEY(" + MATIERE_UE_ID + ") REFERENCES " + TABLE_UE + "(" + UE_ID + ") ON DELETE CASCADE)";

        String CREATE_TABLE_SEANCE = "CREATE TABLE " + TABLE_SEANCE + "("
                + SEANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SEANCE_MATIERE_ID + " INTEGER,"
                + SEANCE_DATE + " TEXT,"
                + SEANCE_HEURE_DEBUT + " TEXT,"
                + SEANCE_DUREE + " INTEGER,"
                + SEANCE_CONTENU_PEDAGOGIQUE + " TEXT,"
                + "FOREIGN KEY(" + SEANCE_MATIERE_ID + ") REFERENCES " + TABLE_MATIERE + "(" + MATIERE_ID + ") ON DELETE CASCADE)";

        db.execSQL(CREATE_TABLE_UE);
        db.execSQL(CREATE_TABLE_MATIERE);
        db.execSQL(CREATE_TABLE_SEANCE);

//        insertDefaultData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_UE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MATIERE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEANCE);
        onCreate(db);
    }

    private void insertDefaultData(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL("INSERT INTO " + TABLE_UE + " (" + UE_CODE + ", " + UE_NOM + ") VALUES ('UE1', 'Administration Système')");
            db.execSQL("INSERT INTO " + TABLE_UE + " (" + UE_CODE + ", " + UE_NOM + ") VALUES ('UE2', 'Communication')");
            db.execSQL("INSERT INTO " + TABLE_UE + " (" + UE_CODE + ", " + UE_NOM + ") VALUES ('UE3', 'Propagation')");
            db.execSQL("INSERT INTO " + TABLE_UE + " (" + UE_CODE + ", " + UE_NOM + ") VALUES ('UE4', 'Electronique')");
            db.execSQL("INSERT INTO " + TABLE_UE + " (" + UE_CODE + ", " + UE_NOM + ") VALUES ('UE5', 'Cellular Networks')");
            db.execSQL("INSERT INTO " + TABLE_UE + " (" + UE_CODE + ", " + UE_NOM + ") VALUES ('UE6', 'Mobile Communication')");
            db.execSQL("INSERT INTO " + TABLE_UE + " (" + UE_CODE + ", " + UE_NOM + ") VALUES ('UE7', 'Internet des objets')");
            db.execSQL("INSERT INTO " + TABLE_UE + " (" + UE_CODE + ", " + UE_NOM + ") VALUES ('UE8', 'Réseaux Mobile')");
            db.execSQL("INSERT INTO " + TABLE_UE + " (" + UE_CODE + ", " + UE_NOM + ") VALUES ('UE9', 'Réseaux Informatiques')");


            db.execSQL("INSERT INTO " + TABLE_MATIERE + " (" + MATIERE_UE_ID + ", " + MATIERE_NOM + ", " + MATIERE_ENSEIGNANT + ", " + MATIERE_VOLUME_HORAIRE + ") VALUES (1, 'Administration Réseaux Sous Linux', 'Dr. Drame', 40)");
            db.execSQL("INSERT INTO " + TABLE_MATIERE + " (" + MATIERE_UE_ID + ", " + MATIERE_NOM + ", " + MATIERE_ENSEIGNANT + ", " + MATIERE_VOLUME_HORAIRE + ") VALUES (1, 'Administration Linux', 'Dr. Gaye Lamine', 30)");
            db.execSQL("INSERT INTO " + TABLE_MATIERE + " (" + MATIERE_UE_ID + ", " + MATIERE_NOM + ", " + MATIERE_ENSEIGNANT + ", " + MATIERE_VOLUME_HORAIRE + ") VALUES (1, 'Systeme dexploitation', 'Dr. Mamadou Dia', 40)");
            db.execSQL("INSERT INTO " + TABLE_MATIERE + " (" + MATIERE_UE_ID + ", " + MATIERE_NOM + ", " + MATIERE_ENSEIGNANT + ", " + MATIERE_VOLUME_HORAIRE + ") VALUES (2, 'Anglais', 'Dr. Alioune Cisse', 20)");
            db.execSQL("INSERT INTO " + TABLE_MATIERE + " (" + MATIERE_UE_ID + ", " + MATIERE_NOM + ", " + MATIERE_ENSEIGNANT + ", " + MATIERE_VOLUME_HORAIRE + ") VALUES (3, 'Antennes et signal', 'Dr. Ibra Dioum', 30)");
            db.execSQL("INSERT INTO " + TABLE_MATIERE + " (" + MATIERE_UE_ID + ", " + MATIERE_NOM + ", " + MATIERE_ENSEIGNANT + ", " + MATIERE_VOLUME_HORAIRE + ") VALUES (3, 'Antenna & Microwave', 'Dr. Ibra Dioum', 35)");
            db.execSQL("INSERT INTO " + TABLE_MATIERE + " (" + MATIERE_UE_ID + ", " + MATIERE_NOM + ", " + MATIERE_ENSEIGNANT + ", " + MATIERE_VOLUME_HORAIRE + ") VALUES (4, 'Système embarqué', 'Dr. Ouya Samuel', 40)");
            db.execSQL("INSERT INTO " + TABLE_MATIERE + " (" + MATIERE_UE_ID + ", " + MATIERE_NOM + ", " + MATIERE_ENSEIGNANT + ", " + MATIERE_VOLUME_HORAIRE + ") VALUES (5, 'From GSM to UMTS', 'Rr. Martin Sauter', 40)");
            db.execSQL("INSERT INTO " + TABLE_MATIERE + " (" + MATIERE_UE_ID + ", " + MATIERE_NOM + ", " + MATIERE_ENSEIGNANT + ", " + MATIERE_VOLUME_HORAIRE + ") VALUES (5, 'GSM to UMTS', 'Rr. Martin Sauter', 40)");



            db.execSQL("INSERT INTO " + TABLE_SEANCE + " (" + SEANCE_MATIERE_ID + ", " + SEANCE_DATE + ", " + SEANCE_HEURE_DEBUT + ", " + SEANCE_DUREE + ", " + SEANCE_CONTENU_PEDAGOGIQUE + ") VALUES (1, '14/05/2026', '16h:00', 2, 'Configuration des protocoles de routage OSPF et tests de connectivité sur simulateur GNS3.')");
            db.execSQL("INSERT INTO " + TABLE_SEANCE + " (" + SEANCE_MATIERE_ID + ", " + SEANCE_DATE + ", " + SEANCE_HEURE_DEBUT + ", " + SEANCE_DUREE + ", " + SEANCE_CONTENU_PEDAGOGIQUE + ") VALUES (8, '15/03/2026', '14h:30', 3, 'Circuit-Switched Data Transmission.')");
            db.execSQL("INSERT INTO " + TABLE_SEANCE + " (" + SEANCE_MATIERE_ID + ", " + SEANCE_DATE + ", " + SEANCE_HEURE_DEBUT + ", " + SEANCE_DUREE + ", " + SEANCE_CONTENU_PEDAGOGIQUE + ") VALUES (8, '07/05/2026', '12h:30', 1, 'Classic Circuit Switching')");
            db.execSQL("INSERT INTO " + TABLE_SEANCE + " (" + SEANCE_MATIERE_ID + ", " + SEANCE_DATE + ", " + SEANCE_HEURE_DEBUT + ", " + SEANCE_DUREE + ", " + SEANCE_CONTENU_PEDAGOGIQUE + ") VALUES (2, '14/02/2026', '15h:00', 3, 'The Mobile Switching Center (MSC), Server, and Gateway')");
            db.execSQL("INSERT INTO " + TABLE_SEANCE + " (" + SEANCE_MATIERE_ID + ", " + SEANCE_DATE + ", " + SEANCE_HEURE_DEBUT + ", " + SEANCE_DUREE + ", " + SEANCE_CONTENU_PEDAGOGIQUE + ") VALUES (2, '14/02/2026', '16h:00', 3, 'The Mobile Switching Center (MSC), Server, and Gateway')");
            db.execSQL("INSERT INTO " + TABLE_SEANCE + " (" + SEANCE_MATIERE_ID + ", " + SEANCE_DATE + ", " + SEANCE_HEURE_DEBUT + ", " + SEANCE_DUREE + ", " + SEANCE_CONTENU_PEDAGOGIQUE + ") VALUES (4, '15/05/2026', '14h:30', 3, 'Le role de la langue Anglaise dans la communication')");
            db.execSQL("INSERT INTO " + TABLE_SEANCE + " (" + SEANCE_MATIERE_ID + ", " + SEANCE_DATE + ", " + SEANCE_HEURE_DEBUT + ", " + SEANCE_DUREE + ", " + SEANCE_CONTENU_PEDAGOGIQUE + ") VALUES (4, '15/05/2026', '13h:30', 2, 'Le wolof et la langue Anglaise')");
            db.execSQL("INSERT INTO " + TABLE_SEANCE + " (" + SEANCE_MATIERE_ID + ", " + SEANCE_DATE + ", " + SEANCE_HEURE_DEBUT + ", " + SEANCE_DUREE + ", " + SEANCE_CONTENU_PEDAGOGIQUE + ") VALUES (8, '05/02/2026', '12h:30', 2, 'GPRS Mobility Management and Session Management (GMM/SM)')");
            db.execSQL("INSERT INTO " + TABLE_SEANCE + " (" + SEANCE_MATIERE_ID + ", " + SEANCE_DATE + ", " + SEANCE_HEURE_DEBUT + ", " + SEANCE_DUREE + ", " + SEANCE_CONTENU_PEDAGOGIQUE + ") VALUES (3, '14/01/2026', '16h:00', 2, 'Universal Mobile Telecommunications System (UMTS) and High-Speed Packet Access (HSPA)')");
            db.execSQL("INSERT INTO " + TABLE_SEANCE + " (" + SEANCE_MATIERE_ID + ", " + SEANCE_DATE + ", " + SEANCE_HEURE_DEBUT + ", " + SEANCE_DUREE + ", " + SEANCE_CONTENU_PEDAGOGIQUE + ") VALUES (3, '15/05/2026', '14h:30', 3, 'Adaptive Multirate (AMR) NB and WB Codecs for Voice Calls')");
            db.execSQL("INSERT INTO " + TABLE_SEANCE + " (" + SEANCE_MATIERE_ID + ", " + SEANCE_DATE + ", " + SEANCE_HEURE_DEBUT + ", " + SEANCE_DUREE + ", " + SEANCE_CONTENU_PEDAGOGIQUE + ") VALUES (8, '10/04/2026', '12h:30', 3, 'Adaptive Modulation and Coding, Transmission Rates, and Multicarrier Operation')");


            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
