package com.example.ndhfos;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ndhfos.Utility.CommonMethods;
import com.google.android.material.snackbar.Snackbar;

public class PhoneActivity extends AppCompatActivity {
	
	private static final String LOG_TAG = PhoneActivity.class.getSimpleName();
	private EditText phoneNumberET;
	private View decorView;
	
	@Override
	protected void onCreate ( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		decorView = getWindow().getDecorView();
		setContentView( R.layout.activity_phone );
		
		CommonMethods.checkInternetAccess( PhoneActivity.this );
		
		phoneNumberET = findViewById( R.id.phoneNumber );
		ImageButton nextIB = findViewById( R.id.next_button );
		
		nextIB.setOnClickListener( ( event ) -> onNext() );
		nextIB.setClickable( false );
		nextIB.setFocusable( false );
		nextIB.setColorFilter(
				getResources().getColor(
						android.R.color.darker_gray,
						getTheme()
				)
		);
		
		phoneNumberET.addTextChangedListener( new TextWatcher() {
			@Override
			public void beforeTextChanged ( CharSequence s, int start, int count, int after ) {
			
			}
			
			@Override
			public void onTextChanged ( CharSequence s, int start, int before, int count ) {
			
			}
			
			@Override
			public void afterTextChanged ( Editable s ) {
				
				if ( s.toString().length() == 10 ) {
					
					nextIB.setClickable( true );
					nextIB.setFocusable( true );
					nextIB.setColorFilter(
							getResources().getColor(
									R.color.colorAccent,
									getTheme()
							)
					);
					
				} else {
					
					nextIB.setClickable( false );
					nextIB.setFocusable( false );
					nextIB.setColorFilter(
							getResources().getColor(
									android.R.color.darker_gray,
									getTheme()
							)
					);
					
				}
				
			}
		} );
		
		phoneNumberET.setOnKeyListener( ( v, keyCode, event ) -> {
			
			if ( event.getAction() == KeyEvent.ACTION_DOWN ) {
				
				switch ( keyCode ) {
					
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						if ( nextIB.isClickable() )
							onNext();
						return true;
					default:
						break;
					
				}
				
			}
			return false;
			
		} );
		
	}
	
	private void onNext () {
		
		CommonMethods.hideSoftInput( PhoneActivity.this );
		
		Snackbar snackbar = Snackbar.make(
				
				findViewById( android.R.id.content ),
				R.string.invalid_number,
				Snackbar.LENGTH_SHORT
		
		);
		snackbar.getView().setBackgroundColor( Color.RED );
		( (TextView) snackbar.getView()
				.findViewById( com.google.android.material.R.id.snackbar_text ) )
				.setTextColor( Color.WHITE );
		
		try {
			
			Long.parseLong( phoneNumberET.getText().toString() );
			
		} catch ( NumberFormatException ex ) {
			
			Log.e( LOG_TAG, phoneNumberET.getText().toString(), ex );
			if ( snackbar.isShown() ) {
				
				snackbar.dismiss();
				snackbar.show();
				return;
				
			}
			
		}
		
		String phoneNumber = "+91" + phoneNumberET.getText().toString();
		String formattedNumber = PhoneNumberUtils.formatNumber( phoneNumber, "91" );
		Log.i( LOG_TAG, "Phone number: " + formattedNumber );
		Spanned verifyingNotice = Html.fromHtml( getString( R.string.verifying_notice, formattedNumber ) );
		
		new AlertDialog.Builder( PhoneActivity.this )
				.setTitle( R.string.check_phone )
				.setMessage( verifyingNotice )
				.setPositiveButton( R.string.ok, ( dialog, which ) -> startIntent( phoneNumber ) )
				.setNegativeButton( R.string.edit, null )
				.create()
				.show();
		
		
	}
	
	private void startIntent ( String phoneNumber ) {
		
		Bundle bundle = new Bundle();
		
		bundle.putString( getString( R.string.pNum ), phoneNumber );
		
		Intent startOTPActivity = new Intent( PhoneActivity.this, OTPActivity.class );
		startOTPActivity.putExtras( bundle );
		startActivity( startOTPActivity );
		overridePendingTransition( android.R.anim.fade_out, android.R.anim.fade_in );
		finish();
		
	}
	
	@Override
	public void finish () {
		super.finish();
		overridePendingTransition( android.R.anim.fade_in, android.R.anim.fade_out );
	}
	
	@Override
	protected void onResume () {
		super.onResume();
		
		decorView.setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE );
		
	}
}
