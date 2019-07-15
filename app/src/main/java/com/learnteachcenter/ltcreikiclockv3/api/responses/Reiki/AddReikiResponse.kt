package com.learnteachcenter.ltcreikiclockv3.api.responses.Reiki

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.learnteachcenter.ltcreikiclockv3.position.Position

data class AddReikiResponse (
    @SerializedName("_id")
    @Expose
    val id: String = "",

    @SerializedName("seqNo")
    @Expose
    val seqNo: Int = 0,

    @SerializedName("title")
    @Expose
    val title: String = "",

    @SerializedName("description")
    @Expose
    val description: String? = null,

    @SerializedName("playMusic")
    @Expose
    val playMusic: Boolean = false,

    @SerializedName("positions")
    @Expose
    val positions: List<Position>? = null
)