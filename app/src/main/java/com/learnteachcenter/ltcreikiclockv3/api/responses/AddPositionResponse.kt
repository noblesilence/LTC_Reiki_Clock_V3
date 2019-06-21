package com.learnteachcenter.ltcreikiclockv3.api.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.learnteachcenter.ltcreikiclockv3.reiki.position.Position

// The Reiki API will return Reiki object,
// which is the same as AddReikiResponse
data class AddPositionResponse (
    @SerializedName("_id")
    @Expose
    val id: String = "",

    @Expose
    val seqNo: Int = 0,

    @Expose
    val title: String = "",

    @Expose
    val duration: String = ""
)