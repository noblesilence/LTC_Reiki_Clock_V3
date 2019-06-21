package com.learnteachcenter.ltcreikiclockv3.reiki.position

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.learnteachcenter.ltcreikiclockv3.reiki.Reiki

@Entity(foreignKeys = arrayOf(ForeignKey(
    entity = Reiki::class,
    parentColumns = arrayOf("id"),
    childColumns = arrayOf("reikiId"),
    onDelete = ForeignKey.CASCADE)),
    tableName = "positions",
    indices = arrayOf(Index(value = ["reikiId"])))
data class Position (
    @Expose @SerializedName("_id") @NonNull @PrimaryKey var id: String,
    @Expose var seqNo: Int,
    var reikiId: String,
    @Expose var title: String,
    @Expose var duration: String
)