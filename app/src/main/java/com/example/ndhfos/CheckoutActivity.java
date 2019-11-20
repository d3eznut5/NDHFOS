package com.example.ndhfos;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.example.ndhfos.Adapters.CheckoutItemAdapter;
import com.example.ndhfos.Database.ItemsDatabase;
import com.example.ndhfos.POJO.Item;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {

    private ItemsDatabase database;

    private CheckoutItemAdapter checkoutItemAdapter;

    private LinearLayout totalCostHolder, confirmBlock;

    private ListView checkoutList;

    private TextView emptyView, orderNumberTV, itemCountTV, totalCostTV;

    private Spinner blockSpinner;

    private RadioGroup paymentOptions;

    private Button placeOrderBT;

    private View separator;

    private List<Item> items;

    private String key;

    private int orderNumber, uiMode;

    private boolean loggedIn;

    private Snackbar snackbar;

    private static final String LOG_TAG = CheckoutActivity.class.getSimpleName();

    private Menu menu;

    private float totalCost;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        Log.i(LOG_TAG, "Inflating Options Menu");

        getMenuInflater().inflate(R.menu.menu_checkout, menu);
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

            placeOrderBT.setText(R.string.sign_in);

        } else {

            menu.findItem(R.id.sign_out).setVisible(true);
            menu.findItem(R.id.sign_in).setVisible(false);
            loggedIn = true;

            placeOrderBT.setText(R.string.checkout);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {

            case R.id.sign_in:
                Intent login = new Intent(CheckoutActivity.this,PhoneActivity.class);
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
                placeOrderBT.setText(R.string.sign_in);
                return true;

            case R.id.dark_mode:
                changeTheme(item);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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

        loggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        Bundle extras = getIntent().getExtras();

        if(extras == null)
            finish();
        else if(extras.containsKey("restaurant"))
            key = extras.getString("restaurant");
        else
            finish();

        Toolbar toolbar = findViewById(R.id.toolbar_cart);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle(R.string.order_details);

        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        checkoutList = findViewById(R.id.list_checkout);

        totalCostHolder = findViewById(R.id.total_cost_holder);
        confirmBlock = findViewById(R.id.confirm_block);

        orderNumberTV = findViewById(R.id.order_number_tv);
        orderNumberTV.setVisibility(View.INVISIBLE);
        itemCountTV = findViewById(R.id.item_count_tv);
        totalCostTV = findViewById(R.id.total_cost_tv);

        blockSpinner = findViewById(R.id.block_spinner);

        paymentOptions = findViewById(R.id.payment_options);

        placeOrderBT = findViewById(R.id.order_button);

        if(loggedIn)
            placeOrderBT.setText(R.string.checkout);
        else
            placeOrderBT.setText(R.string.sign_in);

        emptyView = findViewById(R.id.empty_view);
        separator = findViewById(R.id.separator);

        database = ItemsDatabase.getInstance(CheckoutActivity.this);

        items = database.itemDAO().viewItems();

        checkoutItemAdapter = new CheckoutItemAdapter(CheckoutActivity.this, items);
        checkoutList.setAdapter(checkoutItemAdapter);

        itemCountTV.setText(getString(items.size()==1?
                R.string.number_of_item:R.string.number_of_items,items.size()));
        getOrderNumber();
        getData();

        placeOrderBT.setOnClickListener((event)->{

            if(loggedIn){

                Log.i(LOG_TAG,"Checking out items");

                if(paymentOptions.getCheckedRadioButtonId() == R.id.cod_rbt){

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    Log.i(LOG_TAG,"Got FireStore reference");

                    Map<String, Object> order = new HashMap<>();

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user==null)
                        return;
                    order.put("phoneNumber",user.getPhoneNumber());
                    order.put("block",blockSpinner.getSelectedItem().toString());
                    order.put("total_cost",totalCost);

                    Log.i(LOG_TAG,order.toString());

                    for(Item item : items)
                        order.put(item.getKey(),item.getQuantity());

                    db.collection("restaurants")
                            .document(key)
                            .collection("orders")
                            .document(String.valueOf(orderNumber)).set(order);

                    Spanned orderNotice = Html.fromHtml(getString(R.string.order_message,totalCost));

                    new AlertDialog.Builder(CheckoutActivity.this)
                            .setTitle(R.string.order_placed)
                            .setMessage(orderNotice)
                            .setPositiveButton(R.string.ok,(dialog,which)->CheckoutActivity.this.finish())
                            .create()
                            .show();

                }

            } else {

                Log.i(LOG_TAG,"Starting log in process");
                Intent signIn = new Intent(CheckoutActivity.this, PhoneActivity.class);
                startActivity(signIn);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);

            }

        });

        ArrayAdapter<String> blockAdapter = new ArrayAdapter<>(
                CheckoutActivity.this,
                R.layout.spinner_item,
                getResources().getStringArray(R.array.blocks)
        );

        blockSpinner.setAdapter(blockAdapter);

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

        if(loggedIn)
            getUserDetails();
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
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void getData(){

        database.itemDAO().viewCart().observe(CheckoutActivity.this, items -> {

                if(items.isEmpty()){

                    emptyView.setVisibility(View.VISIBLE);
                    separator.setVisibility(View.INVISIBLE);
                    totalCostHolder.setVisibility(View.INVISIBLE);
                    confirmBlock.setVisibility(View.INVISIBLE);
                    checkoutList.setVisibility(View.INVISIBLE);
                    orderNumberTV.setVisibility(View.INVISIBLE);
                    itemCountTV.setVisibility(View.INVISIBLE);
                    paymentOptions.setVisibility(View.INVISIBLE);
                    placeOrderBT.setVisibility(View.INVISIBLE);

                } else {

                    emptyView.setVisibility(View.INVISIBLE);

                    checkoutItemAdapter = new CheckoutItemAdapter(CheckoutActivity.this, items);
                    if(items.size() != this.items.size()) {
                        checkoutList.setAdapter(checkoutItemAdapter);
                        this.items = items;
                        itemCountTV.setText(getString(items.size()==1?
                                R.string.number_of_item:R.string.number_of_items,items.size()));
                    }

                    totalCost = 0;

                    for(Item item : items)
                        totalCost += (double)item.getQuantity() * item.getPrice();


                    totalCostTV.setText(getString(R.string.price_holder, totalCost));

                }

        });

    }

    private void getOrderNumber(){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("restaurants")
                .document(key)
                .collection("orders")
                .get()
                .addOnCompleteListener((task)->{

                    if(task.getResult()!=null){

                        List<DocumentSnapshot> documents = task.getResult().getDocuments();

                        orderNumber = documents.size()+1;

                        orderNumberTV.setText(getString(R.string.order_number, orderNumber));
                        orderNumberTV.setVisibility(items.size()!=0?View.VISIBLE:View.INVISIBLE);

                    }

                });

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

    private void getUserDetails(){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser =  FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null)
            return;
        db.collection("users").document(currentUser.getUid()).get().addOnCompleteListener((task)->{

            if(task.getResult()!=null){

                DocumentSnapshot currentDocument = task.getResult();

                List<String> blocks = Arrays.asList(getResources().getStringArray(R.array.blocks));

                int index = blocks.indexOf(currentDocument.getString("block"));

                blockSpinner.setSelection(index, true);

            }

        });

    }

}