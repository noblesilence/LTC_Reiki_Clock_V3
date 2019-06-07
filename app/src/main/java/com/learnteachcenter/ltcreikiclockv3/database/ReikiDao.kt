package com.learnteachcenter.ltcreikiclockv3.database

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.learnteachcenter.ltcreikiclockv3.reiki.position.Position
import com.learnteachcenter.ltcreikiclockv3.reiki.one.Reiki
import com.learnteachcenter.ltcreikiclockv3.reiki.session.ReikiAndAllPositions

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

    @Transaction
    @Query("SELECT * FROM reikis WHERE id= :reikiId")
    fun getReiki(reikiId: String): LiveData<ReikiAndAllPositions>

    @Query("SELECT * FROM positions WHERE reikiId = :reikiId ORDER BY seqNo")
    fun getPositions(reikiId: String): LiveData<List<Position>>
}