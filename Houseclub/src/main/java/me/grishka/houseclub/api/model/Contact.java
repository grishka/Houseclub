package me.grishka.houseclub.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Contact {
	public String name, phone_number;

	public Contact(){
	}

	public Contact(String name, String phone_number){
		this.name=name;
		this.phone_number=phone_number;
	}

}
