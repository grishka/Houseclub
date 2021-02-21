package me.grishka.houseclub.api.methods;

import android.net.Uri;
import me.grishka.houseclub.api.BaseResponse;
import me.grishka.houseclub.api.ClubhouseAPIRequest;

public class UpdatePhoto extends ClubhouseAPIRequest<BaseResponse> {
    public UpdatePhoto(Uri file){
        super("POST", "update_photo", BaseResponse.class);
        contentUriToUpload=file;
        fileFieldName="file";
        fileMimeType="image/jpeg";
    }
}