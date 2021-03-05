package me.grishka.houseclub.api.methods;

import java.util.List;

import me.grishka.houseclub.api.ClubhouseAPIRequest;
import me.grishka.houseclub.api.model.SearchUser;

public class SearchPeople extends ClubhouseAPIRequest<SearchPeople.Resp> {

    public SearchPeople(String query) {
        super("POST", "search_users", Resp.class);
        requestBody = new Body(query);
    }

    private static class Body {

        public String query;
        public Body(String query) {
            this.query = query;
        }
    }

    public static class Resp {
        public List<SearchUser> users;
    }
}
