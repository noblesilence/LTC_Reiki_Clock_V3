package com.learnteachcenter.ltcreikiclockv3.reiki

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.learnteachcenter.ltcreikiclockv3.position.Position

@Entity(tableName = "reikis",
    indices = arrayOf(Index(value = ["id"])))
data class Reiki (
    @Expose
    @SerializedName("_id")
    @PrimaryKey @NonNull var id: String,
    @Expose var seqNo: Int,
    @Expose var title: String,
    @Expose var description: String? = "",
    @Expose var playMusic: Boolean = true,
    @Expose @Ignore var positions: List<Position>? = listOf()
) {
    constructor():this("0", 0,"","",false, listOf())
}