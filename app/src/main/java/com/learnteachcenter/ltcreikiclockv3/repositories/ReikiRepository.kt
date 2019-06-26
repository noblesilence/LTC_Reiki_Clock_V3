package com.learnteachcenter.ltcreikiclockv3.repositories

import android.arch.lifecycle.LiveData
import android.os.AsyncTask
import android.util.Log
import com.learnteachcenter.ltcreikiclockv3.app.AppExecutors
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.database.ReikiDao
import com.learnteachcenter.ltcreikiclockv3.util.NetworkUtil
import com.learnteachcenter.ltcreikiclockv3.api.ReikiApi
import com.learnteachcenter.ltcreikiclockv3.api.responses.ApiResponse
import com.learnteachcenter.ltcreikiclockv3.api.responses.DeletePositionResponse
import com.learnteachcenter.ltcreikiclockv3.api.responses.ReikisResponse
import com.learnteachcenter.ltcreikiclockv3.api.responses.DeleteReikiResponse
import com.learnteachcenter.ltcreikiclockv3.reiki.Reiki
import com.learnteachcenter.ltcreikiclockv3.reiki.position.Position
import com.learnteachcenter.ltcreikiclockv3.reiki.session.ReikiAndAllPositions
import com.learnteachcenter.ltcreikiclockv3.util.NetworkBoundResource
import com.learnteachcenter.ltcreikiclockv3.util.Resource
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

interface ReikiRepository {

    fun getReikis(): LiveData<Resource<List<Reiki>>>
    fun getReiki(reikiId: String): LiveData<ReikiAndAllPositions>   // Get one Reiki from the database
    fun updateReikis(vararg reikis: Reiki) // Update the list of Reikis (from reordering operation)
    fun deleteAllReikis()   // Delete all Reikis
    fun deleteReiki(reikiId: String)    // Delete one Reiki in local and remote databases

    fun insertPosition(position: Position)  // Insert 1 Position
    fun updatePosition(position: Position)  // Update 1 Position
    fun updatePositions(vararg positions: Position) // Update a list of Positions (reorder operation)
    fun deletePosition(reikiId: String, positionId: String) // Delete 1 Position (local and remote)
}