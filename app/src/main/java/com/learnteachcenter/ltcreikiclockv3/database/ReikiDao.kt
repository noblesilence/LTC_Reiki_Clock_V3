package com.learnteachcenter.ltcreikiclockv3.database

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import com.learnteachcenter.ltcreikiclockv3.position.Position
import com.learnteachcenter.ltcreikiclockv3.reiki.Reiki
import com.learnteachcenter.ltcreikiclockv3.reiki.ReikiAndAllPositions

@Dao
interface ReikiDao {

    /*
    * Reiki CRUD
    * */
    @Insert(onConflict = REPLACE)
    fun insertReiki(reiki: Reiki): Long

    @Transaction
    @Query("SELECT * FROM reikis WHERE id= :reikiId")
    fun getReiki(reikiId: String): LiveData<ReikiAndAllPositions>

    @Query("SELECT * FROM reikis ORDER BY seqNo")
    fun getReikis(): LiveData<List<Reiki>>

    @Update(onConflict = REPLACE)
    fun updateReiki(reiki: Reiki): Int

    @Update(onConflict = REPLACE)
    fun updateReikis(vararg reikis: Reiki): Int

    @Delete
    fun deleteReikis(vararg reikis: Reiki)

    @Query("DELETE FROM reikis")
    fun deleteAllReikis()

    /*
    * Position CRUD
    * */

    @Insert(onConflict = REPLACE)
    fun insertPosition(position: Position): Long

    @Update(onConflict = REPLACE)
    fun updatePosition(position: Position): Int

    @Update(onConflict = REPLACE)
    fun updatePositions(vararg positions: Position): Int

    @Delete
    fun deletePositions(vararg positions: Position): Int
}