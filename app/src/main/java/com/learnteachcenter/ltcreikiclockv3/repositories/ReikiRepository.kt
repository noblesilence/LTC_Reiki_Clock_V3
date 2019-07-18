package com.learnteachcenter.ltcreikiclockv3.repositories

import android.arch.lifecycle.LiveData
import com.learnteachcenter.ltcreikiclockv3.reiki.Reiki
import com.learnteachcenter.ltcreikiclockv3.position.Position
import com.learnteachcenter.ltcreikiclockv3.reiki.ReikiAndAllPositions
import com.learnteachcenter.ltcreikiclockv3.util.Resource

interface ReikiRepository {

    fun addReiki(reiki: Reiki)
    fun getReikis(): LiveData<Resource<List<Reiki>>>
    fun getReiki(reikiId: String): LiveData<ReikiAndAllPositions>   // Get one Reiki from the database
    fun updateReikis(vararg reikis: Reiki) // Update the list of Reikis (from reordering operation)
    fun deleteReikis(vararg reikis: Reiki)   // Delete the list of Reikis
    fun clearLocalDatabase()

    fun insertPosition(position: Position)  // Insert 1 Position
    fun updatePosition(position: Position)  // Update 1 Position
    fun updatePositions(vararg positions: Position) // Update a list of Positions (reorder operation)
    fun deletePositions(reikiId: String, vararg positions: Position) // Delete 1 Position (local and remote)
}