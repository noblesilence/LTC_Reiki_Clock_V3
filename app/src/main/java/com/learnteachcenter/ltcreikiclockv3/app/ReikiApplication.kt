package com.learnteachcenter.ltcreikiclockv3.app

import android.app.Application
import android.arch.persistence.room.Room
import com.learnteachcenter.ltcreikiclockv3.model.room.ReikiDatabase

class ReikiApplication : Application() {

    companion object {
        lateinit var database: ReikiDatabase
    }

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(this, ReikiDatabase::class.java, "reiki_database")
            .build()
    }
}