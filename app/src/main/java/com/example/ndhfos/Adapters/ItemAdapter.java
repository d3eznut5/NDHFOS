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

import com.example.ndhfos.POJO.Item;
import com.example.ndhfos.R;

import java.util.List;
import java.util.Locale;

public class ItemAdapter extends ArrayAdapter<Item> {

    public ItemAdapter(Context context, List<Item> objects) { super(context, 0, objects);}

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(convertView == null)
            convertView =  ((Activity)getContext()).getLayoutInflater().inflate(R.layout.list_item_item,parent,false);

        TextView itemNameTV = convertView.findViewById(R.id.item_name_tv);
        TextView priceTV = convertView.findViewById(R.id.price_tv);
        ImageView itemImage = convertView.findViewById(R.id.item_image);

        Item item = getItem(position);

        if(item==null)
            return super.getView(position, convertView, parent);

        itemNameTV.setText(item.getName());
        priceTV.setText(
                String.format(
                        Locale.getDefault(),
                        "₹ %.2f",(float)item.getPrice()
                )
        );
        if(item.getImage()!= null)
            itemImage.setImageURI(item.getImage());
        convertView.setTag(item.getKey());

        return convertView;

    }
}
