package me.grishka.houseclub.api

class BaseResponse {
    var success = false
    var errorMessage: String? = null
    override fun toString(): String {
        return "BaseResponse{" +
            "success=" + success +
            ", errorMessage='" + errorMessage + '\'' +
            '}'
    }
}