package me.grishka.houseclub.api.methods

import me.grishka.houseclub.api.ClubhouseAPIRequest
import me.grishka.houseclub.api.model.Channel

class GetChannels : ClubhouseAPIRequest<GetChannels.Response?>("GET", "get_channels", Response::class.java) {
    class Response {
        var channels: List<Channel>? = null
    }
}