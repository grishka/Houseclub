package me.grishka.houseclub.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Club implements Parcelable{

	public int club_id;
	public String name;
	public String description;
	public String photo_url;
	public int num_members;
	public int num_followers;
	public boolean is_member;
	public boolean is_follower;




	@Override
	public int describeContents(){
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags){
		dest.writeInt(this.club_id);
		dest.writeString(this.name);
		dest.writeString(this.description);
		dest.writeString(this.photo_url);
		dest.writeInt(this.num_members);
		dest.writeInt(this.num_followers);
		dest.writeByte(this.is_member ? (byte) 1 : (byte) 0);
		dest.writeByte(this.is_follower ? (byte) 1 : (byte) 0);


	}

	public void readFromParcel(Parcel source){
		this.club_id=source.readInt();
		this.name=source.readString();
		this.description=source.readString();
		this.photo_url=source.readString();
		this.num_members=source.readInt();
		this.num_followers=source.readInt();
		this.is_member=source.readByte()!=0;
		this.is_follower=source.readByte()!=0;

	}

	public Club(){ }

	protected Club(Parcel in){
		this.club_id=in.readInt();
		this.name=in.readString();
		this.description=in.readString();
		this.photo_url=in.readString();
		this.num_members=in.readInt();
		this.num_followers=in.readInt();
		this.is_member=in.readByte()!=0;
		this.is_follower=in.readByte()!=0;


	}

	public static final Creator<Club> CREATOR=new Creator<Club>(){
		@Override
		public Club createFromParcel(Parcel source){
			return new Club(source);
		}

		@Override
		public Club[] newArray(int size){
			return new Club[size];
		}

	};
}
