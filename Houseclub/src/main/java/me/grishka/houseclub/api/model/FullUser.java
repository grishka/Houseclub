package me.grishka.houseclub.api.model;

import java.util.Date;
import java.util.List;

public class FullUser extends User{
	public String dsplayname, bio, twitter, instagram;
	public int numFollowers, numFollowing ;
	public boolean followsMe, isBlockedByNetwork;
	public int mutual_follows_count;
	public Date timeCreated;
	public User invitedByUserProfile;
	public List<FullUser> mutualFollows;
	public List<Club> clubs;

	// null = not following
	// 2 = following
	// other values = ?
	public int notificationType;

	public boolean isFollowed(){
		return notificationType==2;
	}

}
