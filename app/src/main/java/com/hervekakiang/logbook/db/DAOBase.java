package com.hervekakiang.logbook.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class DAOBase<T> {
    protected final static int VERSION = 1;
    protected static final String DATABASE_NAME = "logbook.db";
    protected SQLiteDatabase myDb;
    protected MyDatabaseHelper myDbHelper;

    protected final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public DAOBase(Context context) {
        this.myDbHelper = MyDatabaseHelper.getInstance(context, DATABASE_NAME, VERSION);
        this.myDb = myDbHelper.openDatabase();
    }

//    public void open() {
//        myDb = myDbHelper.getWritableDatabase();
//    }
//
//    public void close() {
//        if (myDb != null && myDb.isOpen()){
//            myDb.close();
//        }
//    }
    public interface Callback<T> {
        void onResult(T result);
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void shutdown() {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
