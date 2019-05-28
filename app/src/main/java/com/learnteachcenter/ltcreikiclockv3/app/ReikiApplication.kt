package com.learnteachcenter.ltcreikiclockv3.app

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import com.learnteachcenter.ltcreikiclockv3.model.datasources.local.ReikiDatabase

class ReikiApplication : Application() {

    companion object {
        lateinit var database: ReikiDatabase
        private lateinit var instance: ReikiApplication

        fun getAppContext(): Context = instance.applicationContext
    }

    override fun onCreate() {
        instance = this
        super.onCreate()

        database = Room.databaseBuilder(this, ReikiDatabase::class.java, "reiki_database")
            .build()
    }
}