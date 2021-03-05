package me.grishka.houseclub.api.methods;

import java.util.HashMap;

import me.grishka.houseclub.api.BaseResponse;
import me.grishka.houseclub.api.ClubhouseAPIRequest;

public class CheckForUpdate extends ClubhouseAPIRequest<BaseResponse> {
    public CheckForUpdate() {
        super("GET", "check_for_update", BaseResponse.class);
        queryParams = new HashMap<>();
        queryParams.put("is_testflight", "0");
    }
}
