package me.grishka.houseclub.api.methods;

import me.grishka.houseclub.api.BaseResponse;
import me.grishka.houseclub.api.ClubhouseAPIRequest;

public class UpdateInstagram extends ClubhouseAPIRequest<BaseResponse> {
	public UpdateInstagram(String token) {
		super("POST", "update_instagram_username", BaseResponse.class);
		requestBody = new Body(token);
	}

	private static class Body{
		public String code;
		Body(String code){
			this.code = code;
		}
	}
}
