package com.georeminder.data.model.other

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.util.DiffUtil
import com.android.databinding.library.baseAdapters.BR
import com.google.gson.annotations.SerializedName

/**
 * Created by Darshna Desai on 17/1/19.
 */
data class ReminderData(@SerializedName("r_id") val id: Int,
                        @SerializedName("r_title") val title: String? = ""
                        , @SerializedName("r_description") val description: String? = ""
                        , @SerializedName("r_latitude") val latitude: String? = ""
                        , @SerializedName("r_longitude") val longitude: String? = ""
                        , @SerializedName("r_radius") val radius: String? = ""
                        , @SerializedName("r_address") val address: String? = ""
                        , @SerializedName("r_items") val items: String? = ""
                        , @SerializedName("r_type") val type: Int
                        , @SerializedName("r_status") var status: Int
                        , @SerializedName("r_created_date") val createdDate: String? = "") :
       BaseObservable(), Parcelable {

    constructor(parcel: Parcel) : this(parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(latitude)
        parcel.writeString(longitude)
        parcel.writeString(radius)
        parcel.writeString(address)
        parcel.writeString(items)
        parcel.writeInt(type)
        parcel.writeInt(status)
        parcel.writeString(createdDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ReminderData> {
        override fun createFromParcel(parcel: Parcel): ReminderData {
            return ReminderData(parcel)
        }

        override fun newArray(size: Int): Array<ReminderData?> {
            return arrayOfNulls(size)
        }

        val diffUtil = object : DiffUtil.ItemCallback<ReminderData>() {
            override fun areItemsTheSame(p0: ReminderData, p1: ReminderData): Boolean {
                return p0.id == p1.id && p0.status == p1.status
            }

            override fun areContentsTheSame(p0: ReminderData, p1: ReminderData): Boolean {
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
        get() = status
        set(value) {
            status = value
            notifyPropertyChanged(BR._all)
        }

    override fun toString() = "$id $status $items $type"
}