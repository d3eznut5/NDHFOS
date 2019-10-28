package com.example.ndhfos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

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

    private static final String LOG_TAG = MenuActivity.class.getSimpleName();
    private static int uiMode;

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

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Bundle extras = getIntent().getExtras();

        if(extras == null || !extras.containsKey("key")){

            Log.e(LOG_TAG,"No key found in intent");
            finish();

        }
        key = getIntent().getExtras().getString("key");

        Toolbar toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.loading);
        errorTV = findViewById(R.id.item_error_tv);
        noMenuTV = findViewById(R.id.no_menu);
        tryAgainBT = findViewById(R.id.try_again_bt);
        itemListView = findViewById(R.id.item_list);

        toolbar.setTitle("Select Items");//TODO: Make unique for each restaurant
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true); TODO: Implement Home Up Button

        errorTV.setVisibility(View.GONE);
        tryAgainBT.setVisibility(View.GONE);
        noMenuTV.setVisibility(View.GONE);

        if(items == null || items.isEmpty())
            getItems();
        else {

            progressBar.setVisibility(View.GONE);
            itemAdapter = new ItemAdapter(MenuActivity.this, items);
            itemListView.setAdapter(itemAdapter);

        }

        tryAgainBT.setOnClickListener((click)->{

            progressBar.setVisibility(View.GONE);
            errorTV.setVisibility(View.GONE);
            tryAgainBT.setVisibility(View.GONE);
            getItems();

        });

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if(uiMode != AppCompatDelegate.getDefaultNightMode())
            recreate();

    }

    @Override
    protected void onResume() {
        super.onResume();
        supportInvalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        Log.i(LOG_TAG,"Inflating the options menu");

        getMenuInflater().inflate(R.menu.menu_menu, menu);
        this.menu = menu;

        //Set icon according to theme
        SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        String mode = preferences.getString("dark_mode",getString(R.string.light_mode));

        MenuItem darkMode = menu.findItem(R.id.dark_mode);
        darkMode.setTitle(mode);

        if(mode.equalsIgnoreCase(getString(R.string.dark_mode)))
            darkMode.setIcon(R.drawable.ic_dark_mode);
        else
            darkMode.setIcon(R.drawable.ic_light_mode);

        //Check if user is logged in and change menu accordingly

        if(FirebaseAuth.getInstance().getCurrentUser() == null ){

            menu.findItem(R.id.sign_out).setVisible(false);
            menu.findItem(R.id.sign_in).setVisible(true);

        } else {

            menu.findItem(R.id.sign_out).setVisible(true);
            menu.findItem(R.id.sign_in).setVisible(false);

        }

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()) {

            case R.id.sign_in:
                Intent login = new Intent(MenuActivity.this,PhoneActivity.class);
                startActivity(login);
                overridePendingTransition(0, 0);
                return true;

            case R.id.sign_out:
                FirebaseAuth.getInstance().signOut();
                Log.i(LOG_TAG, "Signed Out");
                Snackbar.make(getWindow().getDecorView(),
                        "Sign out successful",
                        Snackbar.LENGTH_SHORT)
                        .show();
                menu.findItem(R.id.sign_in).setVisible(true);
                menu.findItem(R.id.sign_out).setVisible(false);
                return true;

            case R.id.dark_mode:
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

        fireStoreDb.collection("restaurants").document(key).collection("menu").get().addOnCompleteListener((task)->{

            if(task.isSuccessful() && task.getResult() != null){

                Log.i(LOG_TAG,"Menu fetch successful");

                for(DocumentSnapshot documentSnapshot : task.getResult().getDocuments()){

                    Item currentItem = new Item(
                            key+ documentSnapshot.getId()
                            ,documentSnapshot.getString("name")
                            ,Integer.parseInt(
                                    ""+documentSnapshot
                                            .getLong("price")
                            )
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
                        items
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
