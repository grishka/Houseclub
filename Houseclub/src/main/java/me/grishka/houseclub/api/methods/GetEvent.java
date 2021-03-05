package me.grishka.houseclub.api.methods;

import me.grishka.houseclub.api.ClubhouseAPIRequest;
import me.grishka.houseclub.api.model.Event;

public class GetEvent extends ClubhouseAPIRequest<GetEvent.Response> {

    public GetEvent(String id) {
        super("POST", "get_event", Response.class);
        requestBody = new Body(id);
    }

    private static class Body {
        public String eventHashid;

        public Body(String eventHashid) {
            this.eventHashid = eventHashid;
        }
    }

    public static class Response {
        public Event event;
    }
}
