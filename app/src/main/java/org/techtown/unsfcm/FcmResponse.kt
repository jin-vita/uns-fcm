package org.techtown.unsfcm

import com.google.gson.annotations.SerializedName

data class FcmResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("message")
    val message: String
) {
    data class Data(
        @SerializedName("body")
        val body: String,
        @SerializedName("dataType")
        val dataType: String,
        @SerializedName("id")
        val id: String,
        @SerializedName("receiver")
        val `receiver`: String,
        @SerializedName("receiverType")
        val receiverType: String,
        @SerializedName("requestCode")
        val requestCode: String,
        @SerializedName("sender")
        val sender: String,
        @SerializedName("title")
        val title: String
    )
}