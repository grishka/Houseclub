package me.grishka.houseclub.api.model;

import android.os.Parcel;

public class ChannelUser extends User {
    public boolean isSpeaker;
    public boolean isModerator;
    public boolean isFollowedBySpeaker;
    public boolean isInvitedAsSpeaker;
    public boolean isNew;
    public String timeJoinedAsSpeaker;
    public String firstName;

    public transient boolean isMuted;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte(this.isSpeaker ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isModerator ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isFollowedBySpeaker ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isInvitedAsSpeaker ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isNew ? (byte) 1 : (byte) 0);
        dest.writeString(this.timeJoinedAsSpeaker);
        dest.writeString(this.firstName);
    }

    public void readFromParcel(Parcel source) {
        super.readFromParcel(source);
        this.isSpeaker = source.readByte() != 0;
        this.isModerator = source.readByte() != 0;
        this.isFollowedBySpeaker = source.readByte() != 0;
        this.isInvitedAsSpeaker = source.readByte() != 0;
        this.isNew = source.readByte() != 0;
        this.timeJoinedAsSpeaker = source.readString();
        this.firstName = source.readString();
    }

    public ChannelUser() {
    }

    protected ChannelUser(Parcel in) {
        super(in);
        this.isSpeaker = in.readByte() != 0;
        this.isModerator = in.readByte() != 0;
        this.isFollowedBySpeaker = in.readByte() != 0;
        this.isInvitedAsSpeaker = in.readByte() != 0;
        this.isNew = in.readByte() != 0;
        this.timeJoinedAsSpeaker = in.readString();
        this.firstName = in.readString();
    }

    public static final Creator<ChannelUser> CREATOR = new Creator<ChannelUser>() {
        @Override
        public ChannelUser createFromParcel(Parcel source) {
            return new ChannelUser(source);
        }

        @Override
        public ChannelUser[] newArray(int size) {
            return new ChannelUser[size];
        }
    };
}
