package me.grishka.houseclub.api.methods

import me.grishka.houseclub.api.BaseResponse
import me.grishka.houseclub.api.ClubhouseAPIRequest

class UpdateName(name: String) : ClubhouseAPIRequest<BaseResponse?>("POST", "update_name", BaseResponse::class.java) {
    private class Body(var name: String)

    init {
        requestBody = Body(name)
    }
}