package com.example.ndhfos.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
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
import com.example.ndhfos.Utility.AppExecutors;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class ItemAdapter extends ArrayAdapter<Item> {

    private ItemsDatabase database;

    private static final String LOG_TAG = ItemAdapter.class.getSimpleName();

    public ItemAdapter(Context context, List<Item> objects) {
        super(context, 0, objects);
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

        Item item = getItem(position);
        database = ItemsDatabase.getInstance(getContext());

        if(item==null)
            return super.getView(position, convertView, parent);

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
                        item.setQuantity(0);
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

        int quantity = item.getQuantity();

        if(quantity == 0){

            addToCartBT.setVisibility(View.VISIBLE);
            increaseQuantityBT.setVisibility(View.INVISIBLE);
            decreaseQuantityBT.setVisibility(View.INVISIBLE);
            quantityTV.setVisibility(View.INVISIBLE);
            deleteButton.setVisibility(View.INVISIBLE);

        } else if (quantity == 1){

            addToCartBT.setVisibility(View.INVISIBLE);
            increaseQuantityBT.setVisibility(View.VISIBLE);
            decreaseQuantityBT.setVisibility(View.INVISIBLE);
            quantityTV.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
            quantityTV.setText(String.valueOf(1));

        } else {


            addToCartBT.setVisibility(View.INVISIBLE);
            increaseQuantityBT.setVisibility(View.VISIBLE);
            decreaseQuantityBT.setVisibility(View.VISIBLE);
            quantityTV.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.INVISIBLE);
            quantityTV.setText(String.valueOf(quantity));

        }

        return convertView;

    }

    private void addToCart(Item item){

        Log.i(LOG_TAG,"Added "+item.getName()+" to cart.");
        item.setQuantity(1);
        AppExecutors.getInstance().getDiskIO().execute(()->database.itemDAO().insertItem(item));

    }

    private int updateCart(Item item, boolean increase){


        int currentQuantity = item.getQuantity()+(increase?1:-1);
        Log.i(LOG_TAG, currentQuantity+" "+item.getName()+"s in cart");
        if(currentQuantity <= 0) {
            AppExecutors.getInstance().getDiskIO().execute(()->database.itemDAO().deleteItem(item));
            currentQuantity = 0;
        } else {
            item.setQuantity(currentQuantity);
            AppExecutors.getInstance().getDiskIO().execute(()->database.itemDAO().updateItem(item));
        }

        return currentQuantity;

    }

    private void deleteItem(Item item){

        item.setQuantity(0);
        AppExecutors.getInstance().getDiskIO().execute(()->database.itemDAO().deleteItem(item));

    }

}
