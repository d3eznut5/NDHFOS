package com.example.ndhfos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.ndhfos.Utility.CommonMethods;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity {

    private String phoneNumber, verificationId;

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private CountDownTimer countDownTimer;

    private EditText otpET;
    private TextView timerTV, resendTV;
    private View decorView;
    private ImageButton confirmIB;
    private ProgressBar progressBar;

    private Snackbar snackbar;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks stateChangedCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendingToken;

    private static final String LOG_TAG = OTPActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        decorView = getWindow().getDecorView();
        setContentView(R.layout.activity_otp);

        Bundle extras = getIntent().getExtras();
        if(extras == null || !extras.containsKey(getString(R.string.pNum))){

            Log.e(LOG_TAG, "Require phone number to request OTP");
            finish();
            return;

        }

        phoneNumber = extras.getString(getString(R.string.pNum));
        Log.i(LOG_TAG, "Phone number: "+phoneNumber);

        otpET = findViewById(R.id.otp);
        timerTV = findViewById(R.id.timer);
        resendTV = findViewById(R.id.resend_otp);
        progressBar = findViewById(R.id.loading);
        confirmIB = findViewById(R.id.confirm_button);
        confirmIB.setOnClickListener((event)->verifyOTP());
        resendTV.setOnClickListener((event)->sendOTP(resendingToken));

        otpET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if(s.toString().length() == 6){

                    confirmIB.setClickable(true);
                    confirmIB.setFocusable(true);
                    confirmIB.setColorFilter(getResources().getColor(R.color.colorAccent, getTheme()));

                } else {

                    confirmIB.setClickable(false);
                    confirmIB.setFocusable(false);
                    confirmIB.setColorFilter(getResources().getColor(android.R.color.darker_gray, getTheme()));

                }

            }
        });

        otpET.setOnKeyListener((v,keyCode,event)->{

            if (event.getAction() == KeyEvent.ACTION_DOWN)
            {
                switch (keyCode)
                {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                        if(confirmIB.isClickable())
                            verifyOTP();
                        return true;
                    default:
                        break;
                }
            }
            return false;
        });

        initFirebaseCallbacks();
        sendOTP();

    }

    @Override
    protected void onResume() {
        super.onResume();

        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

    }

    private void initFirebaseCallbacks(){

        stateChangedCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                Log.d(LOG_TAG, "Verification Successful");
                otpET.setText(phoneAuthCredential.getSmsCode());
                signIn(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                Log.e(LOG_TAG,"Verification Failed",e);
                enableResendOTP();
                if(snackbar.isShown())
                    snackbar.dismiss();
                snackbar = Snackbar.make(findViewById(android.R.id.content),
                        R.string.try_again_no_verify,
                        Snackbar.LENGTH_SHORT
                );
                snackbar.getView().setBackgroundColor(Color.RED);
                ((TextView)snackbar.getView()
                        .findViewById(com.google.android.material.R.id.snackbar_text))
                        .setTextColor(Color.WHITE);
                snackbar.show();

            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {

                snackbar = Snackbar.make(findViewById(android.R.id.content), "OTP sent", Snackbar.LENGTH_SHORT);
                ((TextView)snackbar.getView()
                        .findViewById(com.google.android.material.R.id.snackbar_text))
                        .setTextColor(Color.WHITE);
                snackbar.show();
                verificationId = s;
                resendingToken = forceResendingToken;

            }

        };

    }

    private void updateView(){

        resendTV.setTextColor(getResources().getColor(android.R.color.darker_gray,getTheme()));
        resendTV.setClickable(false);
        resendTV.setFocusable(false);
        resendTV.setVisibility(View.INVISIBLE);

        timerTV.setVisibility(View.VISIBLE);

        confirmIB.setClickable(false);
        confirmIB.setFocusable(false);
        confirmIB.setColorFilter(getResources().getColor(android.R.color.darker_gray,getTheme()));

        countDownTimer = new CountDownTimer(60000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                long minutes = millisUntilFinished/60000;
                long seconds = millisUntilFinished/1000 - minutes;

                String min = minutes<10?"0"+minutes:""+minutes;
                String sec = seconds<10?"0"+seconds:""+seconds;

                String time = min+":"+sec;

                timerTV.setText(time);

            }

            @Override
            public void onFinish() {

                enableResendOTP();

            }
        }.start();


    }

    private void enableResendOTP(){

        countDownTimer.cancel();
        timerTV.setVisibility(View.INVISIBLE);
        resendTV.setTextColor(getResources().getColor(R.color.colorAccent,getTheme()));
        resendTV.setClickable(true);
        resendTV.setFocusable(true);
        resendTV.setVisibility(View.VISIBLE);

    }

    private void sendOTP(){

        updateView();
        PhoneAuthProvider.getInstance(firebaseAuth).verifyPhoneNumber(

                phoneNumber,
                60,
                TimeUnit.SECONDS,
                OTPActivity.this,
                stateChangedCallbacks
        );

    }

    private void sendOTP(PhoneAuthProvider.ForceResendingToken token){

        updateView();
        PhoneAuthProvider.getInstance(firebaseAuth).verifyPhoneNumber(

                phoneNumber,
                60,
                TimeUnit.SECONDS,
                OTPActivity.this,
                stateChangedCallbacks,
                token

        );

    }

    private void verifyOTP(){

        confirmIB.setClickable(false);
        confirmIB.setFocusable(false);
        confirmIB.setColorFilter(getResources().getColor(android.R.color.darker_gray,getTheme()));

        progressBar.setVisibility(View.VISIBLE);

        CommonMethods.hideSoftInput(OTPActivity.this);

        signIn(PhoneAuthProvider.getCredential(verificationId, otpET.getText().toString()));

    }

    private void signIn(PhoneAuthCredential credential){

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, (task)->{

                    if (task.isSuccessful() && task.getResult()!=null) {
                        // Sign in success
                        countDownTimer.cancel();
                        Log.d(LOG_TAG, "signInWithCredential:success");
                        if (snackbar.isShown())
                            snackbar.dismiss();

                        boolean isNewUser = task.getResult().getAdditionalUserInfo() == null || task.getResult().getAdditionalUserInfo().isNewUser();
                        if(isNewUser){

                            Intent i = new Intent(OTPActivity.this, UserDetailsActivity.class);
                            startActivity(i);
                            overridePendingTransition(android.R.anim.fade_out,android.R.anim.fade_in);
                            finish();

                        } else
                            finish();

                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w(LOG_TAG, "signInWithCredential:failure", task.getException());
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                            if (snackbar.isShown())
                                snackbar.dismiss();
                            snackbar = Snackbar.make(findViewById(android.R.id.content),R.string.wrong_otp, Snackbar.LENGTH_SHORT);
                            snackbar.getView().setBackgroundColor(Color.RED);
                            snackbar.show();
                        }

                        confirmIB.setClickable(true);
                        confirmIB.setFocusable(true);
                        confirmIB.setColorFilter(getResources().getColor(R.color.colorAccentLight,getTheme()));

                        progressBar.setVisibility(View.INVISIBLE);

                    }

                });

    }

    @Override
    public void finish(){

        super.finish();
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);

    }


}
