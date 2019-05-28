package com.learnteachcenter.ltcreikiclockv3.model.datasources.local

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.learnteachcenter.ltcreikiclockv3.model.reikis.Position
import com.learnteachcenter.ltcreikiclockv3.model.reikis.Reiki

@Dao
interface ReikiDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReiki(reiki: Reiki): Long

    @Query("SELECT * FROM reikis")
    fun getReikis(): LiveData<List<Reiki>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(reiki: Reiki): Int

    @Query("DELETE FROM reikis")
    fun deleteAllReikis()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPosition(position: Position): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(position: Position): Int

    @Query("SELECT * FROM positions WHERE reikiId = :reikiId ORDER BY seqNo")
    fun getPositions(reikiId: String): List<Position>
}