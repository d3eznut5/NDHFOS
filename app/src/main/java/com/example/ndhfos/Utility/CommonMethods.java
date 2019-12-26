package com.example.ndhfos.Utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.example.ndhfos.R;

import javax.annotation.Nonnull;

public class CommonMethods {
	
	private static final String LOG_TAG = CommonMethods.class.getSimpleName();
	
	public static void checkInternetAccess ( @Nonnull Activity activity ) {
		
		Log.i( LOG_TAG, "Checking for internet access" );
		
		ConnectivityManager manager = (ConnectivityManager)
				activity.getSystemService( Context.CONNECTIVITY_SERVICE );
		NetworkInfo activeNetworkInfo = manager == null ?
				null : manager.getActiveNetworkInfo();
		
		if ( activeNetworkInfo == null || !activeNetworkInfo.isConnected() ) {
			
			AlertDialog.Builder builder = new AlertDialog.Builder( activity )
					.setTitle( activity.getResources()
							.getString( R.string.internet_unavailable ) )
					.setMessage( activity.getResources()
							.getString( R.string.error_message_internet ) )
					.setPositiveButton( activity.getResources()
									.getString( R.string.retry ),
							( ( dialog, which ) -> restartActivity( activity ) ) )
					.setNegativeButton( activity.getResources()
									.getString( R.string.exit ),
							( ( dialog, which ) -> activity.finish() ) )
					.setIcon( activity.getResources()
							.getDrawable( R.drawable.ic_no_internet,
									activity.getTheme() ) );
			
			AlertDialog dialog = builder.create();
			dialog.show();
			
		}
		
	}
	
	private static void restartActivity ( @Nonnull Activity activity ) {
		
		Log.i( LOG_TAG, "Restarting Activity " + activity.getLocalClassName() );
		
		Intent restartActivity = activity.getIntent();
		
		activity.finish();
		activity.overridePendingTransition( 0, 0 );
		activity.startActivity( restartActivity );
		activity.overridePendingTransition( 0, 0 );
		
	}
	
	public static void hideSoftInput ( @Nonnull Activity activity ) {
		
		Log.i( LOG_TAG, "Hiding any soft input if open" );
		
		InputMethodManager inputMethodManager = (InputMethodManager)
				activity.getSystemService( Context.INPUT_METHOD_SERVICE );
		if ( inputMethodManager != null )
			inputMethodManager.hideSoftInputFromWindow(
					activity.getCurrentFocus() == null ?
							null : activity.getCurrentFocus().getWindowToken()
					, InputMethodManager.HIDE_NOT_ALWAYS
			);
		
		
	}
	
}
