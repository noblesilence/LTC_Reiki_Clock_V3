package com.learnteachcenter.ltcreikiclockv3.model.room

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.learnteachcenter.ltcreikiclockv3.model.Position
import com.learnteachcenter.ltcreikiclockv3.model.Reiki

// TODO: export schema: https://developer.android.com/training/data-storage/room/migrating-db-versions.html#top_of_page

@Database(entities = [(Reiki::class), (Position::class)], version = 1)
abstract class ReikiDatabase : RoomDatabase() {
    abstract fun reikiDao(): ReikiDao
}