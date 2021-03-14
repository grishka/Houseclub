package me.grishka.houseclub.api.methods;

import java.util.List;

import me.grishka.houseclub.api.ClubhouseAPIRequest;
import me.grishka.houseclub.api.model.Club;
import me.grishka.houseclub.api.model.Topic;

public class GetClub extends ClubhouseAPIRequest<GetClub.Response>{

	public GetClub(int id){
		super("POST", "get_club", Response.class);
		requestBody=new Body(id);
	}

	private static class Body{
		public int club_id;
		public Body(int club_id){ this.club_id=club_id; }
	}

	public static class Response{
		public Club club;
		public boolean is_admin;
		public boolean is_member;
		public boolean is_follower;
		public List<Topic> topics;
	}

}
