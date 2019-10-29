package com.example.ndhfos.POJO;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "items")
public class Item implements Parcelable {

    @PrimaryKey @NonNull private String key;

    private String name;
    private long price;
    private int quantity;
    @Ignore @androidx.annotation.Nullable
    private Uri image;

    private static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel source) { return new Item(source);}

        @Override
        public Item[] newArray(int size) { return new Item[size];}
    };

    @Ignore
    public Item(@NonNull String key, String name, long price){

        this.key = key;
        this.name = name;
        this.price = price;

    }

    @Ignore
    public Item(@NonNull String key, String name, long price, @Nullable Uri image){

        this(key,name,price);
        this.image = image;

    }

    public Item(@NonNull String key, String name, long price, int quantity){

        this(key,name,price);
        this.quantity = quantity;
        this.image = null;

    }

    @Ignore
    private Item(@NonNull Parcel parcel){

        String key = parcel.readString();
        if(key == null)
            throw new IllegalArgumentException("Key not present in parcel.");
        this.key = key;
        this.name = parcel.readString();
        this.price = parcel.readInt();
        String imageUri = parcel.readString();
        this.image = imageUri == null ? null : Uri.parse(imageUri);

    }

    @Ignore
    @Override
    public int describeContents() { return 0; }

    @Ignore
    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(key);
        dest.writeString(name);
        dest.writeLong(price);
        dest.writeString(image == null ? null : image.toString());

    }

    @NonNull
    public String getKey() { return key; }

    public long getPrice() { return price; }

    public String getName() { return name; }

    @Nullable
    public Uri getImage() { return image; }

    public void setKey(@NonNull String key) { this.key = key; }

    public void setImage(@Nullable Uri image) { this.image = image; }

    public void setName(String name) { this.name = name; }

    public void setPrice(long price) { this.price = price; }

    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }
}
