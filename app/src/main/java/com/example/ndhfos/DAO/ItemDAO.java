package com.example.ndhfos.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ndhfos.POJO.Item;

import java.util.List;

@Dao
public interface ItemDAO {
	
	@Query ( "SELECT * FROM items;" )
	List<Item> viewItems ();
	
	@Query ( "SELECT * FROM items;" )
	LiveData<List<Item>> viewCart ();
	
	@Query ( "SELECT COUNT(*) FROM items;" )
	LiveData<Integer> getNumberOfItems ();
	
	@Insert
	void insertItem ( Item newItem );
	
	@Update ( onConflict = OnConflictStrategy.REPLACE )
	void updateItem ( Item item );
	
	@Delete
	void deleteItem ( Item item );
	
}
