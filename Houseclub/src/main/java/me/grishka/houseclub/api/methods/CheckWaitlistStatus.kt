package me.grishka.houseclub.api.methods

import me.grishka.houseclub.api.ClubhouseAPIRequest

class CheckWaitlistStatus :
    ClubhouseAPIRequest<CheckWaitlistStatus.Response?>("POST", "check_waitlist_status", Response::class.java) {
    class Response(var isWaitlisted:Boolean = false, var isOnboarding:Boolean = false)
}