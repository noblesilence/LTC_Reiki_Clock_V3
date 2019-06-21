package com.learnteachcenter.ltcreikiclockv3.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.learnteachcenter.ltcreikiclockv3.reiki.position.Position
import com.learnteachcenter.ltcreikiclockv3.reiki.Reiki

@Database(entities = [(Reiki::class), (Position::class)], version = 3)
abstract class ReikiDatabase : RoomDatabase() {
    abstract fun reikiDao(): ReikiDao
}