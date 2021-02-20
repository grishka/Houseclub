package me.grishka.houseclub.api.methods

import me.grishka.houseclub.api.BaseResponse
import me.grishka.houseclub.api.ClubhouseAPIRequest

class Follow(userID: Int) : ClubhouseAPIRequest<BaseResponse?>("POST", "follow", BaseResponse::class.java) {
    private class Body(var userId: Int) {
        var source = 4
    }

    init {
        requestBody = Body(userID)
    }
}