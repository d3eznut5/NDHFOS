package com.example.ndhfos.POJO;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Restaurant implements Parcelable {
	
	public static final Creator<Restaurant> CREATOR = new Creator<Restaurant>() {
		@Override
		public Restaurant createFromParcel ( Parcel source ) {
			return new Restaurant( source );
		}
		
		@Override
		public Restaurant[] newArray ( int size ) {
			return new Restaurant[ size ];
		}
	};
	private String key, name;
	@Nullable
	private Uri image;
	
	public Restaurant ( String key, String name, @Nullable Uri image ) {
		
		this.key = key;
		this.name = name;
		this.image = image;
		
	}
	
	private Restaurant ( @Nonnull Parcel parcel ) {
		
		this.key = parcel.readString();
		this.name = parcel.readString();
		String image = parcel.readString();
		this.image = image == null ? null : Uri.parse( image );
		
	}
	
	@Override
	public int describeContents () {
		return 0;
	}
	
	@Override
	public void writeToParcel ( Parcel dest, int flags ) {
		
		dest.writeString( key );
		dest.writeString( name );
		dest.writeString( image == null ? null : image.toString() );
		
	}
	
	//POJO Methods
	
	public String getKey () {
		return key;
	}
	
	public void setKey ( String key ) {
		this.key = key;
	}
	
	public String getName () {
		return name;
	}
	
	public void setName ( String name ) {
		this.name = name;
	}
	
	@Nullable
	public Uri getImage () {
		return image;
	}
	
	public void setImage ( @Nullable Uri image ) {
		this.image = image;
	}
}
