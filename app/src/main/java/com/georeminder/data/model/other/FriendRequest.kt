package com.georeminder.data.model.other

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.util.DiffUtil
import com.android.databinding.library.baseAdapters.BR

/**
 * Created by Darshna Desai on 24/1/19.
 */
data class FriendRequest(val request_id: Int,
                         val user_id: Int,
                         val user_first_name: String? = "",
                         val user_last_name: String? = "",
                         val user_email: String? = "",
                         var request_status: Int,
                         val user_friend_created_date: String? = "",
                         val user_image_url: String? = "",
                         val request_type: Int) : BaseObservable(), Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(request_id)
        parcel.writeInt(user_id)
        parcel.writeString(user_first_name)
        parcel.writeString(user_last_name)
        parcel.writeString(user_email)
        parcel.writeInt(request_status)
        parcel.writeString(user_friend_created_date)
        parcel.writeString(user_image_url)
        parcel.writeInt(request_type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FriendRequest> {
        override fun createFromParcel(parcel: Parcel): FriendRequest {
            return FriendRequest(parcel)
        }

        override fun newArray(size: Int): Array<FriendRequest?> {
            return arrayOfNulls(size)
        }

        val diffUtil = object : DiffUtil.ItemCallback<FriendRequest>() {
            override fun areItemsTheSame(p0: FriendRequest, p1: FriendRequest): Boolean {
                return p0.user_id == p1.user_id && p0.request_status == p1.request_status
            }

            override fun areContentsTheSame(p0: FriendRequest, p1: FriendRequest): Boolean {
                return p0 == p1
            }

        }
    }

    /**
     * setStatus is created by kotlin.
     * so to update whole ui.
     */
    var _status: Int
        @Bindable
        get() = request_status
        set(value) {
            request_status = value
            notifyPropertyChanged(BR._all)
        }

}