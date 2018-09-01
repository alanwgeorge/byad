package com.example.tylerwalker.buyyouadrink.model

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class User(
        var user_id: String,
        var first_name: String = "",
        var last_name: String = "",
        var location: Coordinates = Coordinates(0F, 0F),
        var favorite_drink: String = "",
        var caption: String = "",
        var image_url: String = "",
        var display_name: String = "",
        var email: String = "",
        var phone: String = "",
        var bio: String = "",
        var profile_image: String = "",
        var cover_image: String = ""
): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            Coordinates(parcel.readFloat(), parcel.readFloat()),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(user_id)
        parcel.writeString(first_name)
        parcel.writeString(last_name)
        parcel.writeFloat(location.latitude)
        parcel.writeFloat(location.longitude)
        parcel.writeString(favorite_drink)
        parcel.writeString(caption)
        parcel.writeString(image_url)
        parcel.writeString(display_name)
        parcel.writeString(email)
        parcel.writeString(phone)
        parcel.writeString(bio)
        parcel.writeString(profile_image)
        parcel.writeString(cover_image)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}

data class Coordinates (
        val latitude: Float,
        val longitude: Float
)

sealed class Drink {
    abstract val name: String
    abstract val type: String

    object Coffee: Drink() {
        override val name = "Coffee"
        override val type = "caffeinated"
    }

    object BubbleTea: Drink() {
        override  val name = "Bubble Tea"
        override val type = "caffeinated"
    }

    object Beer: Drink() {
        override val name = "Beer"
        override val type = "alchoholic"
    }

    object Juice: Drink() {
        override val name = "Juice"
        override val type = "healthy"
    }
}
