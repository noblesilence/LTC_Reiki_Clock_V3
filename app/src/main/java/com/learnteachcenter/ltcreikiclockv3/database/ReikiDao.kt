package com.learnteachcenter.ltcreikiclockv3.database

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import com.learnteachcenter.ltcreikiclockv3.reiki.position.Position
import com.learnteachcenter.ltcreikiclockv3.reiki.Reiki
import com.learnteachcenter.ltcreikiclockv3.reiki.session.ReikiAndAllPositions

@Dao
interface ReikiDao {

    @Query("SELECT * FROM reikis ORDER BY seqNo")
    fun getReikis(): LiveData<List<Reiki>>

    @Transaction
    @Query("SELECT * FROM reikis WHERE id= :reikiId")
    fun getReiki(reikiId: String): LiveData<ReikiAndAllPositions>

    @Insert(onConflict = REPLACE)
    fun insertReiki(reiki: Reiki): Long

    @Insert(onConflict = REPLACE)
    fun insertPosition(position: Position): Long

    @Update(onConflict = REPLACE)
    fun updateReiki(reiki: Reiki): Int

    @Update(onConflict = REPLACE)
    fun updatePosition(position: Position): Int

    @Update(onConflict = REPLACE)
    fun updateReikis(vararg reikis: Reiki): Int

    @Update(onConflict = REPLACE)
    fun updatePositions(vararg positions: Position): Int

    @Query("DELETE FROM reikis")
    fun deleteAllReikis()

    @Query("DELETE FROM reikis WHERE id = :reikiId")
    fun deleteReiki(reikiId: String)

    @Query("DELETE FROM positions WHERE reikiId = :reikiId AND id = :positionId")
    fun deletePosition(reikiId: String, positionId: String): Int
}