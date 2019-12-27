package com.example.ndhfos.Utility;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.ndhfos.Database.ItemsDatabase;
import com.example.ndhfos.POJO.Item;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private LiveData<List<Item>> items;

    private static final String LOG_TAG = MainViewModel.class.getSimpleName();

    public MainViewModel(@NonNull Application application) {
        super(application);
        ItemsDatabase db = ItemsDatabase.getInstance(this.getApplication());
        Log.d(LOG_TAG,"Actively retrieving items from the database.");
        items = db.itemDAO().viewCart();
    }

    public LiveData<List<Item>> getItems() {
        return items;
    }
}
