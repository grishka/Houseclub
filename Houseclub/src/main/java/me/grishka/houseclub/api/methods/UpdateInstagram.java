package me.grishka.houseclub.api.methods;

import me.grishka.houseclub.api.BaseResponse;
import me.grishka.houseclub.api.ClubhouseAPIRequest;

public class UpdateInstagram extends ClubhouseAPIRequest<BaseResponse>{
	public static String REDIRECT_INSTAGRAM_URL = "https://www.joinclubhouse.com/callback/instagram";
	public UpdateInstagram(String code) {
		super("POST", "update_instagram_username", BaseResponse.class);
		requestBody = new Body(code);
	}

	private static class Body{
		public String code;
		Body(String code){
			this.code = code;
		}
	}
}
