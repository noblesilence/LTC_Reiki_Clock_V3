package com.learnteachcenter.ltcreikiclockv3.model.room

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.learnteachcenter.ltcreikiclockv3.model.Position
import com.learnteachcenter.ltcreikiclockv3.model.Reiki

@Dao
interface ReikiDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(reiki: Reiki): Long

    @Query("SELECT * FROM reikis")
    fun getReikis(): List<Reiki>

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(reiki: Reiki): Int

    @Query("DELETE FROM reikis")
    fun deleteAllReikis()

    @Query("SELECT * FROM positions WHERE reikiId = :reikiId ORDER BY seqNo")
    fun getPositions(reikiId: String): LiveData<List<Position>>
}