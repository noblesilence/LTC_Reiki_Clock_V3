package com.learnteachcenter.ltcreikiclockv3.model

import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull

data class Position (
    var seqNo: Int,
    val reikiId: String,
    @NonNull
    @PrimaryKey val positionId: String,
    var title: String,
    var duration: String
)