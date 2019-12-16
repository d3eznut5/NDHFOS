package com.example.ndhfos;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class UserDetailsActivity extends AppCompatActivity {

    private Spinner blockSpinner;
    private EditText nameET;
    private final String LOG_TAG = getClass().getSimpleName();
    private ProgressBar progressBar;
    private FloatingActionButton doneFAB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        if(FirebaseAuth.getInstance().getCurrentUser() == null){

            Log.e(LOG_TAG,"Cannot create profile. No user logged in");
            finish();

        }

        blockSpinner = findViewById(R.id.block);
        nameET = findViewById(R.id.name_user);
        doneFAB = findViewById(R.id.done_fab);
        progressBar = findViewById(R.id.loading);
        doneFAB.hide();

        nameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if(s.length() == 0)
                    doneFAB.hide();
                else
                    doneFAB.show();

            }
        });

        nameET.setOnKeyListener((v,keyCode,event)->{

            if(event.getAction() == KeyEvent.ACTION_DOWN){

                switch (keyCode){

                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                        if(doneFAB.isShown())
                            updateUserInfo();
                        return true;
                    default: break;

                }

            }
            return false;

        });

        ArrayAdapter<String> blockSpinnerAdapter = new ArrayAdapter<>(
                UserDetailsActivity.this,
                R.layout.spinner_item,
                getResources().getStringArray(R.array.blocks));

        blockSpinner.setAdapter(blockSpinnerAdapter);

        doneFAB.setOnClickListener((event)->updateUserInfo());

    }

    private void updateUserInfo(){

        progressBar.setVisibility(View.VISIBLE);
        doneFAB.hide();

        StringTokenizer name = new StringTokenizer(nameET.getText().toString());
        String block = blockSpinner.getSelectedItem().toString();

        FirebaseFirestore firestoreDb = FirebaseFirestore.getInstance();

        Map<String, String> user = new HashMap<>();
        user.put("first_name",name.nextToken());
        if(name.hasMoreTokens()){

            for(int i = 1; i < name.countTokens()-1; i++)
                name.nextToken();
            user.put("last_name",name.nextToken());

        }
        user.put("block",block);
        if(FirebaseAuth.getInstance().getCurrentUser() == null){

            Log.e(LOG_TAG, "Cannot create profile. No user logged in");
            return;

        }

        user.put("phoneNumber", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

        firestoreDb.collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .set(user)
                .addOnCompleteListener((documentRef)->{

                    finish();
                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);

                }).addOnFailureListener((exception)->{

                    progressBar.setVisibility(View.INVISIBLE);
                    doneFAB.show();

                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),R.string.error_updating_details,Snackbar.LENGTH_SHORT);
                    ((TextView)snackbar.getView()
                            .findViewById(com.google.android.material.R.id.snackbar_text))
                            .setTextColor(Color.WHITE);
                    snackbar.getView().setBackgroundColor(Color.RED);
                    snackbar.show();

                    Log.e(LOG_TAG,"Exception Occurred: ",exception);

                }
        );

    }

}
