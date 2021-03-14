package me.grishka.houseclub.api.methods;

import java.util.List;

import me.grishka.houseclub.api.ClubhouseAPIRequest;
import me.grishka.houseclub.api.model.Event;

public class GetEvents extends ClubhouseAPIRequest<GetEvents.Response> {
	public GetEvents(){
		super("GET", "get_events", Response.class);
	}

	public static class Response{
		public List<Event> events;
	}
}
