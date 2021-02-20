package me.grishka.houseclub.api.methods

import me.grishka.houseclub.api.BaseResponse
import me.grishka.houseclub.api.ClubhouseAPIRequest

class UpdateBio(bio: String) : ClubhouseAPIRequest<BaseResponse?>("POST", "update_bio", BaseResponse::class.java) {
    private class Body(var bio: String)

    init {
        requestBody = Body(bio)
    }
}