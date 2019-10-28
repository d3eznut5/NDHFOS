package com.example.ndhfos.POJO;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import javax.annotation.Nullable;

public class Item implements Parcelable {

    private String key,name;
    private int price;
    @Nullable private Uri image;

    private static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel source) { return new Item(source);}

        @Override
        public Item[] newArray(int size) { return new Item[size];}
    };

    public Item(String key, String name, int price){

        this.key = key;
        this.name = name;
        this.price = price;
        this.image = null;

    }

    public Item(String key, String name, int price, @Nullable Uri image){

        this(key,name,price);
        this.image = image;

    }

    private Item(Parcel parcel){

        this.key = parcel.readString();
        this.name = parcel.readString();
        this.price = parcel.readInt();
        String imageUri = parcel.readString();
        this.image = imageUri == null ? null : Uri.parse(imageUri);

    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(key);
        dest.writeString(name);
        dest.writeInt(price);
        dest.writeString(image == null ? null : image.toString());

    }

    public String getKey() { return key; }

    public int getPrice() { return price; }

    public String getName() { return name; }

    @Nullable
    public Uri getImage() { return image; }

    public void setKey(String key) { this.key = key; }

    public void setImage(@Nullable Uri image) { this.image = image; }

    public void setName(String name) { this.name = name; }

    public void setPrice(int price) { this.price = price; }
}
