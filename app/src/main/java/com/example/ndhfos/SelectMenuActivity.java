package com.example.ndhfos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.ndhfos.Adapters.RestaurantAdapter;
import com.example.ndhfos.Database.ItemsDatabase;
import com.example.ndhfos.POJO.Restaurant;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class SelectMenuActivity
        extends AppCompatActivity
        implements AdapterView.OnItemClickListener {

    private Menu menu;
    private RestaurantAdapter restaurantAdapter;

    private ProgressBar progressBar;
    private TextView errorTV;
    private Button tryAgainBT;
    private ListView restaurantList;

    private ArrayList<Restaurant> restaurants;

    private static int uiMode;
    private static boolean loggedIn;
    private Snackbar snackbar;

    private static final String LOG_TAG = SelectMenuActivity.class.getSimpleName();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        loggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String setToMode = getSharedPreferences("settings",Context.MODE_PRIVATE)
                .getString("dark_mode",getString(R.string.light_mode));

        uiMode = setToMode.equals(getString(R.string.light_mode))?
                AppCompatDelegate.MODE_NIGHT_NO:
                AppCompatDelegate.MODE_NIGHT_YES;

        Log.i(LOG_TAG,"Changing theme to "+setToMode);

        AppCompatDelegate.setDefaultNightMode(uiMode);

        if(savedInstanceState != null && savedInstanceState.containsKey("restaurants"))
            restaurants = savedInstanceState
                    .getParcelableArrayList("restaurants");

        loggedIn = !(FirebaseAuth.getInstance().getCurrentUser() == null);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_menu);

        //Initialising variables for views
        Toolbar toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.loading);
        errorTV = findViewById(R.id.restaurant_error_tv);
        tryAgainBT = findViewById(R.id.try_again_bt);
        restaurantList = findViewById(R.id.menu_list);

        //Initialising the state of each variable
        toolbar.setTitle("Select Restaurant");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        errorTV.setVisibility(View.INVISIBLE);
        tryAgainBT.setVisibility(View.INVISIBLE);

        if(restaurants == null || restaurants.isEmpty())
            getRestaurants();
        else{

            progressBar.setVisibility(View.INVISIBLE);
            restaurantAdapter = new RestaurantAdapter(SelectMenuActivity.this, restaurants);
            restaurantList.setAdapter(restaurantAdapter);

        }

        tryAgainBT.setOnClickListener((click)->{

            progressBar.setVisibility(View.INVISIBLE);
            errorTV.setVisibility(View.INVISIBLE);
            tryAgainBT.setVisibility(View.INVISIBLE);
            getRestaurants();

        });

        restaurantList.setOnItemClickListener(this);

    }

    @Override
    protected void onResume() {

        super.onResume();
        supportInvalidateOptionsMenu();

        ItemsDatabase.getInstance(getApplicationContext()).clearAllTables();
        Log.i(LOG_TAG, "Table Cleared");

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
    public boolean onCreateOptionsMenu(Menu menu) {

        Log.i(LOG_TAG, "Inflating Options Menu");

        getMenuInflater().inflate(R.menu.menu_select_menu, menu);
        this.menu = menu;

        //Set icon according to theme
        SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        String mode = preferences
                .getString(
                        "dark_mode",
                        getString(R.string.light_mode)
                );

        MenuItem darkMode = menu.findItem(R.id.dark_mode);
        darkMode.setTitle(mode);

        if(mode.equalsIgnoreCase(getString(R.string.dark_mode)))
            darkMode.setIcon(R.drawable.ic_dark_mode);
        else
            darkMode.setIcon(R.drawable.ic_light_mode);

        //Check if user is logged in and change menu accordingly
        if(snackbar !=null)
            snackbar.show();

        if(FirebaseAuth.getInstance().getCurrentUser() == null ){

            menu.findItem(R.id.sign_out).setVisible(false);
            menu.findItem(R.id.sign_in).setVisible(true);
            loggedIn = false;

        } else {

            menu.findItem(R.id.sign_out).setVisible(true);
            menu.findItem(R.id.sign_in).setVisible(false);
            loggedIn = true;
        }

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()) {

            case R.id.sign_in:
                Intent login = new Intent(SelectMenuActivity.this,PhoneActivity.class);
                startActivity(login);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                return true;

            case R.id.sign_out:
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String selectedRestaurant = view.getTag().toString();
        Intent showMenu = new Intent(
                SelectMenuActivity.this,
                MenuActivity.class
        );
        showMenu.putExtra(
                "key",
                selectedRestaurant
        );
        String restaurantName = ((Restaurant)parent.getItemAtPosition(position)).getName();
        showMenu.putExtra(
                "name",
                restaurantName
        );
        startActivityForResult(showMenu,1);
        overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
        );

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

    private void getRestaurants(){

        restaurants = new ArrayList<>();

        FirebaseFirestore fireStoreDb = FirebaseFirestore.getInstance();

        fireStoreDb.collection("restaurants")
                .get()
                .addOnCompleteListener((task)->{

            if(task.isSuccessful() && task.getResult() != null){

                Log.i(LOG_TAG,"Fetched restaurant details successfully");

                for(DocumentSnapshot documentSnapshot : task.getResult().getDocuments()){

                    Restaurant currentRestaurant = new Restaurant(
                            documentSnapshot.getId(),
                            documentSnapshot.getString("name"),
                            null
                    );

                    restaurants.add(currentRestaurant);

                }

                Log.i(LOG_TAG, "Inflating ListView with data fetched");
                progressBar.setVisibility(View.INVISIBLE);
                restaurantAdapter = new RestaurantAdapter(
                        SelectMenuActivity.this,
                        restaurants
                );

                restaurantList.setAdapter(restaurantAdapter);

            } else {

                //Error Fetching data
                Log.e(LOG_TAG, "Error fetching data", task.getException());
                progressBar.setVisibility(View.INVISIBLE);
                errorTV.setVisibility(View.VISIBLE);
                tryAgainBT.setVisibility(View.VISIBLE);

            }

        });


    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        outState.putParcelableArrayList("restaurants",restaurants);
        super.onSaveInstanceState(outState);

    }

    @Override
    public void recreate() {
        finish();
        startActivity(getIntent());
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        super.recreate();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }
}