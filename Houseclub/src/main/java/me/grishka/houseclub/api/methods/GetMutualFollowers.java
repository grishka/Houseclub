package me.grishka.houseclub.api.methods;

import java.util.HashMap;
import java.util.List;

import me.grishka.houseclub.api.ClubhouseAPIRequest;
import me.grishka.houseclub.api.model.FullUser;

public class GetMutualFollowers extends ClubhouseAPIRequest<GetMutualFollowers.Response>{
	public GetMutualFollowers(int userID, int pageSize, int page){
		super("GET", "get_mutual_follows", Response.class);
		queryParams=new HashMap<>();
		queryParams.put("user_id", userID+"");
		queryParams.put("page_size", pageSize+"");
		queryParams.put("page", page+"");
	}

	public static class Response{
		public List<FullUser> users;
		public int count;
	}
}
