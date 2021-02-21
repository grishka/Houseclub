package me.grishka.houseclub.api.methods

import me.grishka.houseclub.api.ClubhouseAPIRequest
import me.grishka.houseclub.api.model.FullUser
import java.util.HashMap

class GetFollowers(userID: Int, pageSize: Int, page: Int) :
    ClubhouseAPIRequest<GetFollowers.Response?>("GET", "get_followers", Response::class.java) {
    class Response {
        var users: List<FullUser>? = null
        var count = 0
    }

    init {
        queryParams["user_id"] = userID.toString()
        queryParams["page_size"] = pageSize.toString()
        queryParams["page"] = page.toString()
    }
}