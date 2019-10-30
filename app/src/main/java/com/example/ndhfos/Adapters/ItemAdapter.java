package com.example.ndhfos.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ndhfos.Database.ItemsDatabase;
import com.example.ndhfos.POJO.Item;
import com.example.ndhfos.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class ItemAdapter extends ArrayAdapter<Item> {

    private ItemsDatabase database;
    private Menu menu;
    private TextView cartItemCountTV;

    private static final String LOG_TAG = ItemAdapter.class.getSimpleName();

    public ItemAdapter(Context context, List<Item> objects, Menu menu) {
        super(context, 0, objects);
        this.menu = menu;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(convertView == null)
            convertView =  ((Activity)getContext()).getLayoutInflater().inflate(R.layout.list_item_item,parent,false);

        TextView itemNameTV = convertView.findViewById(R.id.item_name_tv);
        TextView priceTV = convertView.findViewById(R.id.price_tv);
        TextView quantityTV = convertView.findViewById(R.id.current_quantity_tv);

        ImageView itemImage = convertView.findViewById(R.id.item_image);

        Button addToCartBT = convertView.findViewById(R.id.add_to_cart_bt);
        Button increaseQuantityBT = convertView.findViewById(R.id.increase_quantity);
        Button decreaseQuantityBT = convertView.findViewById(R.id.decrease_quantity);
        ImageButton deleteButton = convertView.findViewById(R.id.remove_from_cart);

        MenuItem cartItem = menu.findItem(R.id.add_to_cart);

        View actionView = cartItem.getActionView();
        cartItemCountTV = actionView.findViewById(R.id.cart_badge);

        Item item = getItem(position);
        database = ItemsDatabase.getInstance(getContext());

        if(item==null)
            return super.getView(position, convertView, parent);

        List<Item> cart = database.itemDAO().viewItems();

        addToCartBT.setOnClickListener((event)->{
            addToCart(item);
            addToCartBT.setVisibility(View.INVISIBLE);
            increaseQuantityBT.setVisibility(View.VISIBLE);
            quantityTV.setVisibility(View.VISIBLE);
            decreaseQuantityBT.setVisibility(View.INVISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
            quantityTV.setText(String.valueOf(1));
        });

        increaseQuantityBT.setOnClickListener((event)->{
            int currentQuantity = updateCart(item,true);
            quantityTV.setText(String.valueOf(currentQuantity));
            decreaseQuantityBT.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.INVISIBLE);
        });

        decreaseQuantityBT.setOnClickListener((event)->{
            int currentQuantity = updateCart(item,false);
            if(currentQuantity == 0){
                increaseQuantityBT.setVisibility(View.INVISIBLE);
                decreaseQuantityBT.setVisibility(View.INVISIBLE);
                quantityTV.setVisibility(View.INVISIBLE);
                deleteButton.setVisibility(View.INVISIBLE);
                addToCartBT.setVisibility(View.VISIBLE);
            } else if (currentQuantity == 1) {

                deleteButton.setVisibility(View.VISIBLE);
                decreaseQuantityBT.setVisibility(View.INVISIBLE);
                quantityTV.setText("1");

            } else {

                quantityTV.setText(String.valueOf(item.getQuantity()));
            }
        });

        deleteButton.setOnClickListener((event)->

            new AlertDialog.Builder(getContext())
                    .setTitle("Delete Item")
                    .setMessage("Are you sure you want to remove "+item.getName()+" from the cart?")
                    .setPositiveButton("Yes, please",(dialog,which)->{

                        deleteItem(item);
                        quantityTV.setText("1");
                        increaseQuantityBT.setVisibility(View.INVISIBLE);
                        decreaseQuantityBT.setVisibility(View.INVISIBLE);
                        quantityTV.setVisibility(View.INVISIBLE);
                        deleteButton.setVisibility(View.INVISIBLE);
                        addToCartBT.setVisibility(View.VISIBLE);

                        Snackbar itemDeleted = Snackbar.make(((Activity)getContext()).findViewById(android.R.id.content)
                                , "Removed "+item.getName()+" from cart.",
                                Snackbar.LENGTH_SHORT
                        );

                        itemDeleted.getView().setBackgroundColor(Color.RED);
                        ((TextView)itemDeleted.getView()
                                .findViewById(com.google.android.material.R.id.snackbar_text))
                                .setTextColor(Color.WHITE);
                        itemDeleted.show();

                    })
                    .setNegativeButton("No, I have changed my mind",null)
                    .create()
                    .show()

        );

        itemNameTV.setText(item.getName());
        priceTV.setText(
                getContext().getResources().getString(

                        R.string.price_holder,
                        (double)item.getPrice()

                )
        );
        if(item.getImage()!= null)
            itemImage.setImageURI(item.getImage());
        convertView.setTag(item.getKey());

        if(!cart.isEmpty()){

            addToCartBT.setVisibility(View.VISIBLE);
            increaseQuantityBT.setVisibility(View.INVISIBLE);
            decreaseQuantityBT.setVisibility(View.INVISIBLE);
            quantityTV.setVisibility(View.INVISIBLE);
            deleteButton.setVisibility(View.INVISIBLE);
            cartItemCountTV.setVisibility(View.VISIBLE);

            cartItemCountTV.setText(String.valueOf(cart.size()));

            for(Item currentItem : cart){

                if(currentItem.getKey().equals(item.getKey())){

                    addToCartBT.setVisibility(View.INVISIBLE);
                    increaseQuantityBT.setVisibility(View.VISIBLE);
                    decreaseQuantityBT.setVisibility(View.VISIBLE);
                    quantityTV.setVisibility(View.VISIBLE);
                    deleteButton.setVisibility(View.VISIBLE);
                    quantityTV.setText(String.valueOf(currentItem.getQuantity()));
                    if(currentItem.getQuantity() > 1){

                        deleteButton.setVisibility(View.INVISIBLE);
                        decreaseQuantityBT.setVisibility(View.VISIBLE);

                    } else {

                        deleteButton.setVisibility(View.VISIBLE);
                        decreaseQuantityBT.setVisibility(View.INVISIBLE);

                    }

                }

            }

        } else {

            addToCartBT.setVisibility(View.VISIBLE);
            increaseQuantityBT.setVisibility(View.INVISIBLE);
            decreaseQuantityBT.setVisibility(View.INVISIBLE);
            quantityTV.setVisibility(View.INVISIBLE);
            deleteButton.setVisibility(View.INVISIBLE);
            cartItemCountTV.setVisibility(View.INVISIBLE);

        }

        return convertView;

    }

    private void addToCart(Item item){

        Log.i(LOG_TAG,"Added "+item.getName()+" to cart.");
        item.setQuantity(1);
        database.itemDAO().insertItem(item);
        int currentItems = Integer.parseInt(cartItemCountTV.getText().toString());
        cartItemCountTV.setVisibility(View.VISIBLE);
        cartItemCountTV.setText(String.valueOf(currentItems+1));
    }

    private int updateCart(Item item, boolean increase){


        int currentQuantity = item.getQuantity()+(increase?1:-1);
        Log.i(LOG_TAG, currentQuantity+" "+item.getName()+"s in cart");
        if(currentQuantity <= 0) {
            database.itemDAO().deleteItem(item);
            currentQuantity = 0;
        } else {
            item.setQuantity(currentQuantity);
            database.itemDAO().updateItem(item);
        }

        int itemsCount = database.itemDAO().getNumberOfItems();

        if(itemsCount == 0) {
            cartItemCountTV.setVisibility(View.INVISIBLE);
            cartItemCountTV.setText(String.valueOf(0));
        } else {

            cartItemCountTV.setText(String.valueOf(Math.min(itemsCount, 99)));
            cartItemCountTV.setVisibility(View.VISIBLE);

        }

        return currentQuantity;

    }

    private void deleteItem(Item item){

        item.setQuantity(0);
        database.itemDAO().deleteItem(item);

    }

}
