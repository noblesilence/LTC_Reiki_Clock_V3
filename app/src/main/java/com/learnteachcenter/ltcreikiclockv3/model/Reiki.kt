package com.learnteachcenter.ltcreikiclockv3.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull

@Entity(tableName = "reiki_table")
data class Reiki (
    @PrimaryKey @NonNull val title: String,
    val description: String = "",
    val playMusic: Boolean = true
)