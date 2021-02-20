package me.grishka.houseclub.api.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
open class User(var userId:Int = 0,
                var name: String? = null,
                var photoUrl: String? = null,
                var username: String? = null) : Parcelable {

}