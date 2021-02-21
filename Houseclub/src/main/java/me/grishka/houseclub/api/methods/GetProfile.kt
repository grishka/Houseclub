package me.grishka.houseclub.api.methods

import me.grishka.houseclub.api.ClubhouseAPIRequest
import me.grishka.houseclub.api.model.FullUser

class GetProfile(id: Int) : ClubhouseAPIRequest<GetProfile.Response?>("POST", "get_profile", Response::class.java) {
    private class Body(var userId: Int)
    class Response(var userProfile: FullUser? = null)

    init {
        requestBody = Body(id)
    }
}