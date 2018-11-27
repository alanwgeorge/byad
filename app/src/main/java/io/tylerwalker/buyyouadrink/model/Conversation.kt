package io.tylerwalker.buyyouadrink.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class Conversation(
        val with: String,
        val withId: String,
        val withImage: String,
        val initiated: Timestamp,
        val isInitiatedByMe: Boolean,
        val location: Coordinates,
        val locationName: String,
        val beverageType: String,
        val placeName: String,
        val invitationMessage: String,
        val messages: List<Message> = listOf(),
        val isAccepted: Boolean,
        val isRejected: Boolean
) : Parcelable {



    constructor(parcel: Parcel): this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            Timestamp(parcel.readLong(), 0),
            parcel.readByte() != 0.toByte(),
            Coordinates(parcel.readFloat(), parcel.readFloat()),
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            readMessages(parcel),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, p1: Int) {
        parcel.writeString(with)
        parcel.writeString(withId)
        parcel.writeString(withImage)
        parcel.writeLong(initiated.seconds)
        parcel.writeByte(if (isInitiatedByMe) 1 else 0)
        parcel.writeFloat(location.latitude)
        parcel.writeFloat(location.longitude)
        parcel.writeString(locationName)
        parcel.writeString(beverageType)
        parcel.writeString(placeName)
        parcel.writeString(invitationMessage)
        parcel.writeTypedList(messages)
        parcel.writeByte(if (isAccepted) 1 else 0)
        parcel.writeByte(if (isRejected) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Conversation> {
        override fun createFromParcel(parcel: Parcel): Conversation {
            return Conversation(parcel)
        }

        override fun newArray(size: Int): Array<Conversation?> {
            return arrayOfNulls(size)
        }

        fun readMessages(parcel: Parcel): List<Message> {
            val whatever = mutableListOf<Message>()
            run { parcel.readList(whatever, Message::class.java.classLoader) }
            return whatever
        }
    }
}

data class Message(val from: String, val timestamp: Timestamp, val body: String, val isInvitation: Boolean = false)
    : Parcelable {
    constructor(parcel: Parcel): this(
            parcel.readString() ?: "",
            Timestamp(parcel.readLong(), 0),
            parcel.readString() ?: "",
            parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, p1: Int) {
        parcel.writeString(from)
        parcel.writeLong(timestamp.seconds)
        parcel.writeString(body)
        parcel.writeByte(if (isInvitation) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Message> {
        override fun createFromParcel(parcel: Parcel): Message {
            return Message(parcel)
        }

        override fun newArray(size: Int): Array<Message?> {
            return arrayOfNulls(size)
        }
    }
}