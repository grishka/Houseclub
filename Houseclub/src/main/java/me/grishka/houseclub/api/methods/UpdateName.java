package me.grishka.houseclub.api.methods;

import me.grishka.houseclub.api.BaseResponse;
import me.grishka.houseclub.api.ClubhouseAPIRequest;

public class UpdateName extends ClubhouseAPIRequest<BaseResponse> {
    public UpdateName(String name) {
        super("POST", "update_name", BaseResponse.class);
        requestBody = new Body(name);
    }

    private static class Body {
        public String name;

        public Body(String name) {
            this.name = name;
        }
    }
}
