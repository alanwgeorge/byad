package io.tylerwalker.buyyouadrink.model

import android.os.Parcel
import android.os.Parcelable

data class User(
        var user_id: String = "",
        var location: Coordinates = Coordinates(0F, 0F),
        var caption: String = "",
        var display_name: String = "",
        var email: String = "",
        var phone: String = "",
        var bio: String = "",
        var drinks: String = "",
        var profile_image: String = "",
        var cover_image: String = "",
        var conversations: Any? = null,
        var favoriteDrink: String = ""

): Parcelable {
    constructor(parcel: Parcel) : this(
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
        parcel.writeFloat(location.latitude)
        parcel.writeFloat(location.longitude)
        parcel.writeString(caption)
        parcel.writeString(display_name)
        parcel.writeString(email)
        parcel.writeString(phone)
        parcel.writeString(bio)
        parcel.writeString(drinks)
        parcel.writeString(profile_image)
        parcel.writeString(cover_image)
        parcel.writeString(favoriteDrink)
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

data class
Coordinates (
        val latitude: Float,
        val longitude: Float
)

sealed class Drink {
    abstract val name: String
    abstract val type: String
    var isSelected: Boolean = false

    object Coffee: Drink() {
        override val name = "Coffee"
        override val type = "caffeinated"
    }

    object BubbleTea: Drink() {
        override  val name = "BubbleTea"
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

    companion object {
        fun getDrink(drinkName: String): Drink {
            return when(drinkName) {
                "Coffee" -> Drink.Coffee
                "BubbleTea" -> Drink.BubbleTea
                "Beer" -> Drink.Beer
                "Juice" -> Drink.Juice
                else -> Drink.Coffee
            }
        }
    }
}
