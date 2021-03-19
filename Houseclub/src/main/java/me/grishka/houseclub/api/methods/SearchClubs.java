package me.grishka.houseclub.api.methods;

import java.util.List;

import me.grishka.houseclub.api.ClubhouseAPIRequest;
import me.grishka.houseclub.api.model.Club;

public class SearchClubs extends ClubhouseAPIRequest<SearchClubs.Response> {
	public SearchClubs(String query) {
		super("POST", "search_clubs", Response.class);
		requestBody = new Body(query);
	}

	private static class Body {
		public String query;

		public Body(String query) {
			this.query = query;
		}
	}

	public static class Response{
		public List<Club> clubs;
	}
}
