package com.example.ndhfos.Database;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.ndhfos.DAO.ItemDAO;
import com.example.ndhfos.POJO.Item;

@Database ( entities = { Item.class }, version = 1, exportSchema = false )
public abstract class ItemsDatabase extends RoomDatabase {
	
	private static final String LOG_TAG = ItemsDatabase.class.getSimpleName();
	private static final Object LOCK = new Object();
	private static final String DATABASE_NAME = "cart";
	private static ItemsDatabase instance;
	
	public static ItemsDatabase getInstance ( Context context ) {
		
		if ( instance == null ) {
			
			synchronized ( LOCK ) {
				
				Log.i( LOG_TAG, "Creating new database instance" );
				instance = Room.databaseBuilder(
						
						context.getApplicationContext(),
						ItemsDatabase.class,
						ItemsDatabase.DATABASE_NAME
				
				).build();
				
			}
			
		}
		
		Log.i( LOG_TAG, "Getting the database instance" );
		return instance;
		
	}
	
	public abstract ItemDAO itemDAO ();
	
}
