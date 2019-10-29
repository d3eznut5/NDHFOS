package com.example.ndhfos.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ndhfos.POJO.Item;

import java.util.List;

@Dao
public interface ItemDAO{

    @Query("SELECT * FROM items;")
    List<Item> viewItems();

    @Insert
    void insertItem(Item newItem);

    @Update (onConflict = OnConflictStrategy.REPLACE)
    void updateItem(Item item);

    @Delete
    void deleteItem(Item item);

}
