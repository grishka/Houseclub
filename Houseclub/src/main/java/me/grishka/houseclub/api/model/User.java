package me.grishka.houseclub.api.model;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    public int userId;
    public String name;
    public String photoUrl;
    public String username;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.userId);
        dest.writeString(this.name);
        dest.writeString(this.photoUrl);
        dest.writeString(this.username);
    }

    public void readFromParcel(Parcel source) {
        this.userId = source.readInt();
        this.name = source.readString();
        this.photoUrl = source.readString();
        this.username = source.readString();
    }

    public User() {
    }

    protected User(Parcel in) {
        this.userId = in.readInt();
        this.name = in.readString();
        this.photoUrl = in.readString();
        this.username = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
