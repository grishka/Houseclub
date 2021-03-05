package me.grishka.houseclub.api.model;

import java.util.Date;

public class FullUser extends User {
    public String dsplayname, bio, twitter, instagram;
    public int numFollowers, numFollowing;
    public boolean followsMe, isBlockedByNetwork;
    public Date timeCreated;
    public User invitedByUserProfile;
    // null = not following
    // 2 = following
    // other values = ?
    public int notificationType;

    public boolean isFollowed() {
        return notificationType == 2;
    }
}
