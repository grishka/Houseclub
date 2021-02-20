package me.grishka.houseclub.api.methods

import me.grishka.houseclub.api.ClubhouseAPIRequest
import me.grishka.houseclub.api.model.Channel

class GetChannel(name: String) : ClubhouseAPIRequest<Channel?>("POST", "get_channel", Channel::class.java) {
    private class Body(var channel: String)

    init {
        requestBody = Body(name)
    }
}