package me.grishka.houseclub.api.model

import android.os.Parcel
import android.os.Parcelable

open class User : Parcelable {
    var userId = 0
    var name: String? = null
    var photoUrl: String? = null
    var username: String? = null
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(userId)
        dest.writeString(name)
        dest.writeString(photoUrl)
        dest.writeString(username)
    }

    open fun readFromParcel(source: Parcel) {
        userId = source.readInt()
        name = source.readString()
        photoUrl = source.readString()
        username = source.readString()
    }

    constructor() {}
    protected constructor(`in`: Parcel) {
        userId = `in`.readInt()
        name = `in`.readString()
        photoUrl = `in`.readString()
        username = `in`.readString()
    }

    companion object {
        val CREATOR: Parcelable.Creator<User> = object : Parcelable.Creator<User> {
            override fun createFromParcel(source: Parcel): User {
                return User(source)
            }

            override fun newArray(size: Int): Array<User?> {
                return arrayOfNulls(size)
            }
        }
    }
}