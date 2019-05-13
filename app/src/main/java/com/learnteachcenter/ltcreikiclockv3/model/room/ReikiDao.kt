package com.learnteachcenter.ltcreikiclockv3.model.room

import android.arch.persistence.room.*
import com.learnteachcenter.ltcreikiclockv3.model.Reiki

@Dao
interface ReikiDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(reiki: Reiki): Long

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(reiki: Reiki): Int

    @Delete
    fun clearReikis(vararg reiki: Reiki)

    @Query("SELECT * FROM reiki_table")
    fun getAllReikis(): List<Reiki>
}