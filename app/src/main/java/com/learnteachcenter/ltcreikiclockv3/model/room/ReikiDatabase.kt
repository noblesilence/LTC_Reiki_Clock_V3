package com.learnteachcenter.ltcreikiclockv3.model.room

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.learnteachcenter.ltcreikiclockv3.model.Position
import com.learnteachcenter.ltcreikiclockv3.model.Reiki

@Database(entities = [(Reiki::class), (Position::class)], version = 1)
abstract class ReikiDatabase : RoomDatabase() {
    abstract fun reikiDao(): ReikiDao
}