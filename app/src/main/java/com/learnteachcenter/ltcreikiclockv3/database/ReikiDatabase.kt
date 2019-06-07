package com.learnteachcenter.ltcreikiclockv3.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.learnteachcenter.ltcreikiclockv3.reiki.position.Position
import com.learnteachcenter.ltcreikiclockv3.reiki.one.Reiki

@Database(entities = [(Reiki::class), (Position::class)], version = 2)
abstract class ReikiDatabase : RoomDatabase() {
    abstract fun reikiDao(): ReikiDao
}