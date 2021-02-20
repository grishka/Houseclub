package me.grishka.houseclub.api.methods

import me.grishka.houseclub.api.BaseResponse
import me.grishka.houseclub.api.ClubhouseAPIRequest
import java.util.HashMap

class CheckForUpdate : ClubhouseAPIRequest<BaseResponse?>("GET", "check_for_update", BaseResponse::class.java) {
    init {
        queryParams = HashMap()
        queryParams!!["is_testflight"] = "0"
    }
}