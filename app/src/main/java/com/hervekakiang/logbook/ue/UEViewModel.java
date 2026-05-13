package com.hervekakiang.logbook.ue;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class UEViewModel extends AndroidViewModel {
    private final UEDAO ueDao;

    private final MutableLiveData<List<UE>> listUEs = new MutableLiveData<>();

    public UEViewModel(@NonNull Application application) {
        super(application);
        ueDao = new UEDAO(application);
        refreshList();
    }

    public void addUE(UE ue) {
        ueDao.insert(ue, this::refreshList);
    }

    public LiveData<List<UE>> getListUEs() {
        return listUEs;
    }

    private void refreshList(){
        ueDao.getAll(listUEs::postValue);
        Log.d("UEViewModel", "refreshList is called");
    }
}