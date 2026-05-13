package com.hervekakiang.logbook.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public abstract class DAOBase {
    protected final static int VERSION = 1;
    protected static final String DATABASE_NAME = "logbook.db";
    protected SQLiteDatabase myDb = null;
    protected MyDatabaseHelper myDbHelper;

    public DAOBase(Context context) {
        this.myDbHelper = new MyDatabaseHelper(context, DATABASE_NAME, null, VERSION);
    }

    public void open() {
        myDb = myDbHelper.getWritableDatabase();
    }

    public void close() {
        if (myDb != null && myDb.isOpen()){
            myDb.close();
        }
    }
}
