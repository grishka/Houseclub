package me.grishka.houseclub.api.methods;

import java.util.HashMap;
import java.util.List;

import me.grishka.houseclub.api.ClubhouseAPIRequest;
import me.grishka.houseclub.api.model.Notification;

public class GetNotifications extends ClubhouseAPIRequest<GetNotifications.Response> {
    public GetNotifications(int userID, int pageSize, int page) {
        super("GET", "get_notifications", Response.class);
        queryParams = new HashMap<>();
        queryParams.put("user_id", userID + "");
        queryParams.put("page_size", pageSize + "");
        queryParams.put("page", page + "");
    }

    public static class Response {
        public List<Notification> notifications;
        public int count;
    }
}
