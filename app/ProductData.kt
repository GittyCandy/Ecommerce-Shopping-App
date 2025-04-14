package com.example.myecommerceapp2

import android.os.Parcel
import android.os.Parcelable

data class ProductData(
    val name: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val rating: Float = 0.0f,
    val category: String = "",
    val imageUrl: String = "", // Changed from imageRes: Int
    val specifications: Map<String, String> = emptyMap(),
    val type: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.readFloat(),
        parcel.readString() ?: "",
        parcel.readString() ?: "", // Changed from parcel.readInt()
        parcel.readHashMap(String::class.java.classLoader) as Map<String, String>,
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeDouble(price)
        parcel.writeString(description)
        parcel.writeFloat(rating)
        parcel.writeString(category)
        parcel.writeString(imageUrl) // Changed from parcel.writeInt(imageRes)
        parcel.writeMap(specifications)
        parcel.writeString(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProductData> {
        override fun createFromParcel(parcel: Parcel): ProductData {
            return ProductData(parcel)
        }

        override fun newArray(size: Int): Array<ProductData?> {
            return arrayOfNulls(size)
        }
    }
}