package com.example.ndhfos.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ndhfos.POJO.Restaurant;
import com.example.ndhfos.R;

import java.util.ArrayList;

public class RestaurantAdapter extends ArrayAdapter<Restaurant> {

    public RestaurantAdapter(Context context, ArrayList<Restaurant> objects){super(context,0,objects);}

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(convertView == null)
            convertView = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.list_item_menu,parent, false);

        TextView restaurantNameTV = convertView.findViewById(R.id.restaurant_name_tv);
        ImageView restaurantImage = convertView.findViewById(R.id.restaurant_image);

        Restaurant restaurant = getItem(position);

        if(restaurant == null)
            return  super.getView(position,convertView,parent);

        restaurantNameTV.setText(restaurant.getName());
        if(restaurant.getImage()!=null)
            restaurantImage.setImageURI(restaurant.getImage());
        convertView.setTag(restaurant.getKey());

        return convertView;

    }
}
