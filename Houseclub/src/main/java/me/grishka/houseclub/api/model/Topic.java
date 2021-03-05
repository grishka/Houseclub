package me.grishka.houseclub.api.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Topic implements Parcelable{
	/*

"topics": [
  {
"title": "📈 Marketing",
"id": 112,
"abbreviated_title": "Marketing"
},


]
	* */

	public String title;
	public int id;
	public String abbreviated_title;



	@Override
	public int describeContents(){
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags){
		dest.writeString(this.title);
		dest.writeInt(this.id);
		dest.writeString(this.abbreviated_title);
	}

	public void readFromParcel(Parcel source){
		this.title=source.readString();
		this.id=source.readInt();
		this.abbreviated_title=source.readString();
	}

	public Topic(){}

	protected Topic(Parcel in){
		this.title=in.readString();
		this.id=in.readInt();
		this.abbreviated_title=in.readString();
	}

	public static final Creator<Topic> CREATOR=new Creator<Topic>(){
		@Override
		public Topic createFromParcel(Parcel source){
			return new Topic(source);
		}

		@Override
		public Topic[] newArray(int size){
			return new Topic[size];
		}
	};
}
