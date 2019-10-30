package com.example.ndhfos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.ndhfos.Adapters.ItemAdapter;
import com.example.ndhfos.POJO.Item;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MenuActivity extends AppCompatActivity {

    private Menu menu;

    private ProgressBar progressBar;
    private TextView errorTV, noMenuTV;
    private Button tryAgainBT;
    private ListView itemListView;

    private ItemAdapter itemAdapter;
    private ArrayList<Item> items;

    private String key;
    private Snackbar snackbar;

    private static final String LOG_TAG = MenuActivity.class.getSimpleName();
    private static int uiMode;
    private static boolean loggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String setToMode = getSharedPreferences("settings",Context.MODE_PRIVATE)
                .getString("dark_mode",getString(R.string.light_mode));

        uiMode = setToMode.equals(getString(R.string.light_mode))?
                AppCompatDelegate.MODE_NIGHT_NO:
                AppCompatDelegate.MODE_NIGHT_YES;

        Log.i(LOG_TAG,"Changing theme to "+setToMode);

        AppCompatDelegate.setDefaultNightMode(uiMode);

        if(savedInstanceState != null && savedInstanceState.containsKey("items"))
            items = savedInstanceState
                    .getParcelableArrayList("items");

        loggedIn = !(FirebaseAuth.getInstance().getCurrentUser() == null);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Bundle extras = getIntent().getExtras();

        if(extras == null || !extras.containsKey("key")){

            Log.e(LOG_TAG,"No key found in intent");
            finish();
            return;

        }
        key = extras.getString("key");
        String name = extras.getString("name");

        Toolbar toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.loading);
        errorTV = findViewById(R.id.item_error_tv);
        noMenuTV = findViewById(R.id.no_menu);
        tryAgainBT = findViewById(R.id.try_again_bt);
        itemListView = findViewById(R.id.item_list);

        toolbar.setTitle(name);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        errorTV.setVisibility(View.GONE);
        tryAgainBT.setVisibility(View.GONE);
        noMenuTV.setVisibility(View.GONE);

        tryAgainBT.setOnClickListener((click)->{

            progressBar.setVisibility(View.GONE);
            errorTV.setVisibility(View.GONE);
            tryAgainBT.setVisibility(View.GONE);
            getItems();

        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        supportInvalidateOptionsMenu();

        if(uiMode != AppCompatDelegate.getDefaultNightMode())
            recreate();

        if(!loggedIn && !(FirebaseAuth.getInstance().getCurrentUser() == null)){

            snackbar = Snackbar.make(findViewById(android.R.id.content),getString(R.string.sign_in_successful),Snackbar.LENGTH_SHORT);
            snackbar.getView().setBackgroundColor(getColor(R.color.signInSnackbarBackground));
            ((TextView)snackbar.getView()
                    .findViewById(com.google.android.material.R.id.snackbar_text))
                    .setTextColor(Color.WHITE);
            loggedIn = true;

        } else
            snackbar = null;

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        this.menu = menu;

        if(items == null || items.isEmpty())
            getItems();
        else {

            progressBar.setVisibility(View.GONE);
            itemAdapter = new ItemAdapter(MenuActivity.this, items, menu);
            itemListView.setAdapter(itemAdapter);

        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        Log.i(LOG_TAG,"Inflating the options menu");

        getMenuInflater().inflate(R.menu.menu_menu, menu);

        //Set icon according to theme
        SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        String mode = preferences.getString("dark_mode",getString(R.string.light_mode));

        menu.findItem(R.id.add_to_cart).getActionView().setOnClickListener((event)->{

            Log.i(LOG_TAG,"Starting checkout process");
            Intent checkout = new Intent(MenuActivity.this, CheckoutActivity.class);
            startActivity(checkout);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        });

        MenuItem darkMode = menu.findItem(R.id.dark_mode_menu);
        darkMode.setTitle(mode);

        if(mode.equalsIgnoreCase(getString(R.string.dark_mode)))
            darkMode.setIcon(R.drawable.ic_dark_mode);
        else
            darkMode.setIcon(R.drawable.ic_light_mode);

        //Check if user is logged in and change menu accordingly
        if(snackbar != null)
            snackbar.show();

        if(FirebaseAuth.getInstance().getCurrentUser() == null ){

            loggedIn = false;
            menu.findItem(R.id.sign_out_menu).setVisible(false);
            menu.findItem(R.id.sign_in_menu).setVisible(true);

        } else {
            loggedIn = true;
            menu.findItem(R.id.sign_out_menu).setVisible(true);
            menu.findItem(R.id.sign_in_menu).setVisible(false);

        }

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        Log.i(LOG_TAG,item.getTitle().toString());

        switch(item.getItemId()) {

            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(MenuActivity.this);
                return true;

            case R.id.sign_in_menu:
                Intent login = new Intent(MenuActivity.this,PhoneActivity.class);
                startActivity(login);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;

            case R.id.sign_out_menu:
                FirebaseAuth.getInstance().signOut();
                Log.i(LOG_TAG, "Signed Out");
                Snackbar signOut = Snackbar.make(findViewById(android.R.id.content),
                        "Sign out successful",
                        Snackbar.LENGTH_SHORT);
                signOut.getView().setBackgroundColor(getColor(R.color.signOutSnackbarBackground));
                ((TextView)signOut.getView()
                        .findViewById(com.google.android.material.R.id.snackbar_text))
                        .setTextColor(Color.WHITE);
                signOut.show();
                loggedIn = false;
                menu.findItem(R.id.sign_in_menu).setVisible(true);
                menu.findItem(R.id.sign_out_menu).setVisible(false);
                return true;

            case R.id.dark_mode_menu:
                changeTheme(item);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    private void changeTheme(MenuItem item){

        String mode = item.getTitle().toString();

        if(mode.equalsIgnoreCase(getString(R.string.light_mode)))
            mode = getString(R.string.dark_mode);
        else
            mode = getString(R.string.light_mode);

        SharedPreferences preferences = getSharedPreferences(
                "settings",
                Context.MODE_PRIVATE
        );
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("dark_mode",mode);
        editor.apply();

        item.setTitle(mode);

        Log.i(LOG_TAG, "Theme changed to "+mode);

        recreate();

    }

    private void getItems(){

        items = new ArrayList<>();

        FirebaseFirestore fireStoreDb = FirebaseFirestore.getInstance();

        fireStoreDb.collection("restaurants")
                .document(key)
                .collection("menu")
                .get()
                .addOnCompleteListener((task)->{

                    if(task.isSuccessful() && task.getResult() != null){

                        Log.i(LOG_TAG,"Menu fetch successful");

                        for(DocumentSnapshot documentSnapshot : task.getResult().getDocuments()){

                            Long price = documentSnapshot.getLong("price");

                            Item currentItem = new Item(
                                    documentSnapshot.getId()
                                    ,documentSnapshot.getString("name")
                                    ,price == null ? 0 : price
                            );
                            items.add(currentItem);

                        }

                        progressBar.setVisibility(View.GONE);
                        if(items.isEmpty()){

                            Log.i(LOG_TAG, "Menu unavailable");
                            noMenuTV.setVisibility(View.VISIBLE);
                            return;

                        }

                        Log.i(LOG_TAG, "Inflating ListView with data fetched");
                        itemAdapter = new ItemAdapter(
                                MenuActivity.this,
                                items,
                                menu
                        );

                        itemListView.setAdapter(itemAdapter);

                    } else {

                        Log.e(LOG_TAG, "Error fetching data", task.getException());
                        progressBar.setVisibility(View.GONE);
                        errorTV.setVisibility(View.VISIBLE);
                        tryAgainBT.setVisibility(View.VISIBLE);

                    }

                });

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArrayList("items",items);
        super.onSaveInstanceState(outState);
    }
}
