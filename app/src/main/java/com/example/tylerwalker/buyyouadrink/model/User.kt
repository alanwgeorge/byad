package com.example.tylerwalker.buyyouadrink.model

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class User(
        val user_id: Int,
        val first_name: String,
        val last_name: String,
        val location: Coordinates,
        val favorite_drink: String,
        val caption: String = "",
        val image_url: String? = null
): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            Coordinates(parcel.readFloat(), parcel.readFloat()),
            parcel.readString(),
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(user_id)
        parcel.writeString(first_name)
        parcel.writeString(last_name)
        parcel.writeFloat(location.latitude)
        parcel.writeFloat(location.longitude)
        parcel.writeString(favorite_drink)
        parcel.writeString(caption)
        parcel.writeString(image_url)
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

sealed class Drink() {
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
